package nadiendev.rusticrevived.blockentity.alchemy;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.menu.alchemy.BrewingBarrelMenu;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.recipe.FluidRecipeInput;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Brewing barrel (legacy TileEntityBrewingBarrel): slowly ferments the input tank into the
 * output tank. The culture tank holds booze of the result type that steers the quality of the
 * brew. Each tank has a container slot above it (filled / emptied automatically) and a result
 * slot below it.
 */
public class BrewingBarrelBlockEntity extends SyncedBlockEntity implements MenuProvider {
	public static final int INPUT_IN_SLOT = 0;
	public static final int OUTPUT_IN_SLOT = 1;
	public static final int CULTURE_IN_SLOT = 2;
	public static final int INPUT_OUT_SLOT = 3;
	public static final int OUTPUT_OUT_SLOT = 4;
	public static final int CULTURE_OUT_SLOT = 5;
	public static final int SLOT_COUNT = 6;
	public static final int TANK_CAPACITY = 8000;
	public static final int CULTURE_CAPACITY = 1000;

	/** Brew times are sent to the menu in 15 bit chunks (data slots are shorts). */
	public static final int DATA_BREW_TIME_LOW = 0;
	public static final int DATA_BREW_TIME_HIGH = 1;
	public static final int DATA_MAX_BREW_TIME_LOW = 2;
	public static final int DATA_MAX_BREW_TIME_HIGH = 3;
	public static final int DATA_COUNT = 4;
	private static final int CHUNK_BITS = 15;
	private static final int CHUNK_MASK = (1 << CHUNK_BITS) - 1;

	private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	};
	private final IItemHandlerModifiable automation = new AutomationHandler();
	private final FluidTank input = syncedTank(TANK_CAPACITY);
	private final FluidTank output = syncedTank(TANK_CAPACITY);
	private final FluidTank culture = syncedTank(CULTURE_CAPACITY);

	@Nullable
	private RecipeHolder<BrewingRecipe> recipe;
	private int brewTime;

	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_BREW_TIME_LOW -> brewTime & CHUNK_MASK;
				case DATA_BREW_TIME_HIGH -> brewTime >> CHUNK_BITS;
				case DATA_MAX_BREW_TIME_LOW -> getMaxBrewTime() & CHUNK_MASK;
				case DATA_MAX_BREW_TIME_HIGH -> getMaxBrewTime() >> CHUNK_BITS;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			// read only: the menu keeps its own copy on the client
		}

		@Override
		public int getCount() {
			return DATA_COUNT;
		}
	};

	public BrewingBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BREWING_BARREL.get(), pos, state);
	}

	private FluidTank syncedTank(int capacity) {
		return new FluidTank(capacity) {
			@Override
			protected void onContentsChanged() {
				sync();
			}
		};
	}

	/** Combines the two 15 bit chunks of a brew time sent through the menu data slots. */
	public static int joinChunks(int low, int high) {
		return (high << CHUNK_BITS) | (low & CHUNK_MASK);
	}

	// ---------------------------------------------------------------- accessors

	public ItemStackHandler getItems() {
		return items;
	}

	/** Item handler for automation: only valid containers go in, anything comes out. */
	public IItemHandler getAutomationHandler() {
		return automation;
	}

	public FluidTank getInput() {
		return input;
	}

	public FluidTank getOutput() {
		return output;
	}

	public FluidTank getCulture() {
		return culture;
	}

	public ContainerData getData() {
		return data;
	}

	public static int getMaxBrewTime() {
		return RusticConfig.COMMON.maxBrewTime.get();
	}

	/** Whether the stack may be put into the given slot (menu and automation). */
	public static boolean isItemValid(int slot, ItemStack stack) {
		return switch (slot) {
			case INPUT_IN_SLOT, OUTPUT_IN_SLOT -> isFluidContainer(stack);
			case CULTURE_IN_SLOT -> isFluidContainer(stack) && FluidUtil.getFluidContained(stack).map(BrewingBarrelBlockEntity::isBooze).orElse(true);
			default -> false;
		};
	}

	public static boolean isFluidContainer(ItemStack stack) {
		return stack.is(Items.GLASS_BOTTLE) || FluidUtil.getFluidHandler(stack.copyWithCount(1)).isPresent();
	}

	public static boolean isBooze(FluidStack fluid) {
		return fluid.getFluidType() instanceof BoozeFluidType;
	}

	// ---------------------------------------------------------------- ticking

	public static void serverTick(Level level, BlockPos pos, BlockState state, BrewingBarrelBlockEntity barrel) {
		barrel.tickServer();
	}

	private void tickServer() {
		boolean fluidChanged = tryTankIO(input, INPUT_IN_SLOT, INPUT_OUT_SLOT, true, false)
				| tryTankIO(output, OUTPUT_IN_SLOT, OUTPUT_OUT_SLOT, false, false)
				| tryTankIO(culture, CULTURE_IN_SLOT, CULTURE_OUT_SLOT, true, true);
		if (fluidChanged) {
			brewTime = 0;
			if (recipe != null && !matches(recipe)) {
				recipe = null;
			}
		}
		if (recipe == null && !input.isEmpty() || recipe != null && brewTime % 20 == 0) {
			recipe = findRecipe();
		}
		if (recipe != null) {
			brewTime++;
			if (brewTime >= getMaxBrewTime()) {
				brew(recipe);
				brewTime = 0;
				recipe = null;
			}
			setChanged();
		} else if (brewTime != 0) {
			brewTime = 0;
			setChanged();
		}
	}

	/**
	 * Moves fluid between a tank and the container in its input slot, putting the resulting
	 * container into the matching output slot. Glass bottles are treated as empty fluid bottles.
	 */
	private boolean tryTankIO(FluidTank tank, int inSlot, int outSlot, boolean allowFill, boolean onlyBooze) {
		ItemStack stack = items.getStackInSlot(inSlot);
		IFluidHandlerItem handler = containerHandler(stack);
		if (handler == null) return false;
		FluidStack contained = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
		boolean sameFluid = !contained.isEmpty() && FluidStack.isSameFluidSameComponents(contained, tank.getFluid());

		// fill the tank from the container
		if (allowFill && !contained.isEmpty() && (tank.isEmpty() || sameFluid) && (!onlyBooze || isBooze(contained))) {
			FluidStack drained = handler.drain(tank.fill(contained, IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.EXECUTE);
			if (!drained.isEmpty() && moveContainer(inSlot, outSlot, handler.getContainer())) {
				tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
				return true;
			}
		}

		// fill the container from the tank
		if (!tank.isEmpty() && (contained.isEmpty() || sameFluid)) {
			handler = containerHandler(stack);
			int amount = handler == null ? 0 : handler.fill(tank.getFluid(), IFluidHandler.FluidAction.EXECUTE);
			if (amount > 0 && moveContainer(inSlot, outSlot, handler.getContainer())) {
				tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
				return true;
			}
		}
		return false;
	}

	@Nullable
	private static IFluidHandlerItem containerHandler(ItemStack stack) {
		if (stack.isEmpty()) return null;
		ItemStack container = stack.is(Items.GLASS_BOTTLE) ? new ItemStack(ModItems.FLUID_BOTTLE.get()) : stack.copyWithCount(1);
		return FluidUtil.getFluidHandler(container).orElse(null);
	}

	private boolean moveContainer(int inSlot, int outSlot, ItemStack result) {
		if (!items.insertItem(outSlot, result, true).isEmpty()) return false;
		items.extractItem(inSlot, 1, false);
		items.insertItem(outSlot, result, false);
		return true;
	}

	private boolean matches(RecipeHolder<BrewingRecipe> holder) {
		return holder.value().matches(new FluidRecipeInput(input.getFluid()), level) && holder.value().matchesCulture(culture.getFluid());
	}

	@Nullable
	private RecipeHolder<BrewingRecipe> findRecipe() {
		if (input.isEmpty() || level == null) return null;
		for (RecipeHolder<BrewingRecipe> holder : level.getRecipeManager().getRecipesFor(ModRecipes.BREWING.get(),
				new FluidRecipeInput(input.getFluid()), level)) {
			BrewingRecipe candidate = holder.value();
			if (!candidate.matchesCulture(culture.getFluid())) continue;
			if (output.isEmpty() || candidate.result().getFluid() == output.getFluid().getFluid() && output.getSpace() > 0) {
				return holder;
			}
		}
		return null;
	}

	private void brew(RecipeHolder<BrewingRecipe> holder) {
		int amount = Math.min(output.getSpace(), input.getFluidAmount());
		if (amount <= 0 || level == null || !matches(holder)) return;
		FluidStack result = holder.value().brew(culture.getFluid(), amount, level.random);
		if (!output.isEmpty() && output.getFluid().getFluid() == result.getFluid()) {
			// joins the brew already in the output tank
			result = output.getFluid().copyWithAmount(amount);
		}
		if (output.fill(result, IFluidHandler.FluidAction.SIMULATE) == amount
				&& input.drain(amount, IFluidHandler.FluidAction.SIMULATE).getAmount() == amount) {
			output.fill(result, IFluidHandler.FluidAction.EXECUTE);
			input.drain(amount, IFluidHandler.FluidAction.EXECUTE);
		}
	}

	public void dropContents() {
		dropContents(items);
	}

	// ---------------------------------------------------------------- menu

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.rusticrevived.brewing_barrel");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new BrewingBarrelMenu(containerId, playerInventory, this, data);
	}

	// ---------------------------------------------------------------- persistence

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("Items", items.serializeNBT(registries));
		tag.put("InputTank", input.writeToNBT(registries, new CompoundTag()));
		tag.put("OutputTank", output.writeToNBT(registries, new CompoundTag()));
		tag.put("CultureTank", culture.writeToNBT(registries, new CompoundTag()));
		tag.putInt("BrewTime", brewTime);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		items.deserializeNBT(registries, tag.getCompound("Items"));
		input.readFromNBT(registries, tag.getCompound("InputTank"));
		output.readFromNBT(registries, tag.getCompound("OutputTank"));
		culture.readFromNBT(registries, tag.getCompound("CultureTank"));
		brewTime = tag.getInt("BrewTime");
	}

	/** The barrel inventory as seen by automation (legacy ExternalItemHandler). */
	private final class AutomationHandler implements IItemHandlerModifiable {
		@Override
		public int getSlots() {
			return items.getSlots();
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return items.getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return isItemValid(slot, stack) ? items.insertItem(slot, stack, simulate) : stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return items.extractItem(slot, amount, simulate);
		}

		@Override
		public int getSlotLimit(int slot) {
			return items.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return BrewingBarrelBlockEntity.isItemValid(slot, stack);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			items.setStackInSlot(slot, stack);
		}
	}
}
