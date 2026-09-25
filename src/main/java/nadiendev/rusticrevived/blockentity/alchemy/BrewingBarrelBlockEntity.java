package nadiendev.rusticrevived.blockentity.alchemy;

import org.jspecify.annotations.Nullable;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.menu.alchemy.BrewingBarrelMenu;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.recipe.FluidRecipeInput;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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

	private final AlchemyItems items = new AlchemyItems(SLOT_COUNT, slot -> setChanged());
	private final ResourceHandler<ItemResource> automation = new AutomationItemHandler(items, BrewingBarrelBlockEntity::isItemValid);
	private final AlchemyTank input = new AlchemyTank(TANK_CAPACITY, this::sync);
	private final AlchemyTank output = new AlchemyTank(TANK_CAPACITY, this::sync);
	private final AlchemyTank culture = new AlchemyTank(CULTURE_CAPACITY, this::sync);

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

	/** Combines the two 15 bit chunks of a brew time sent through the menu data slots. */
	public static int joinChunks(int low, int high) {
		return (high << CHUNK_BITS) | (low & CHUNK_MASK);
	}

	// ---------------------------------------------------------------- accessors

	public AlchemyItems getItems() {
		return items;
	}

	/** Item handler for automation: only valid containers go in, anything comes out. */
	public ResourceHandler<ItemResource> getAutomationHandler() {
		return automation;
	}

	public AlchemyTank getInput() {
		return input;
	}

	public AlchemyTank getOutput() {
		return output;
	}

	public AlchemyTank getCulture() {
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
			case CULTURE_IN_SLOT -> {
				FluidStack contained = stack.is(Items.GLASS_BOTTLE) ? FluidStack.EMPTY : FluidUtil.getFirstStackContained(stack);
				yield isFluidContainer(stack) && (contained.isEmpty() || isBooze(contained));
			}
			default -> false;
		};
	}

	public static boolean isFluidContainer(ItemStack stack) {
		return stack.is(Items.GLASS_BOTTLE) || !stack.isEmpty() && ItemAccess.forStack(stack.copyWithCount(1)).getCapability(Capabilities.Fluid.ITEM) != null;
	}

	public static boolean isBooze(FluidStack fluid) {
		return fluid.getFluidType() instanceof BoozeFluidType;
	}

	// ---------------------------------------------------------------- ticking

	public static void serverTick(Level level, BlockPos pos, BlockState state, BrewingBarrelBlockEntity barrel) {
		barrel.tickServer((ServerLevel) level);
	}

	private void tickServer(ServerLevel level) {
		boolean fluidChanged = tryTankIO(input, INPUT_IN_SLOT, INPUT_OUT_SLOT, true, false)
				| tryTankIO(output, OUTPUT_IN_SLOT, OUTPUT_OUT_SLOT, false, false)
				| tryTankIO(culture, CULTURE_IN_SLOT, CULTURE_OUT_SLOT, true, true);
		if (fluidChanged) {
			brewTime = 0;
			if (recipe != null && !matches(level, recipe)) {
				recipe = null;
			}
		}
		if (recipe == null && !input.isEmpty() || recipe != null && brewTime % 20 == 0) {
			recipe = findRecipe(level);
		}
		if (recipe != null) {
			brewTime++;
			if (brewTime >= getMaxBrewTime()) {
				brew(level, recipe);
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
	private boolean tryTankIO(AlchemyTank tank, int inSlot, int outSlot, boolean allowFill, boolean onlyBooze) {
		ItemStack stack = items.getStackInSlot(inSlot);
		if (stack.isEmpty()) return false;
		ItemStack container = stack.is(Items.GLASS_BOTTLE) ? new ItemStack(ModItems.FLUID_BOTTLE.get()) : stack.copyWithCount(1);
		// the container lives in a one slot inventory so that its transformation can be read back
		ItemStacksResourceHandler holder = new ItemStacksResourceHandler(NonNullList.of(ItemStack.EMPTY, container));
		ResourceHandler<FluidResource> handler = ItemAccess.forHandlerIndexStrict(holder, 0).getCapability(Capabilities.Fluid.ITEM);
		if (handler == null) return false;
		FluidStack contained = contents(handler);
		boolean sameFluid = !contained.isEmpty() && FluidStack.isSameFluidSameComponents(contained, tank.getFluid());

		// fill the tank from the container
		if (allowFill && !contained.isEmpty() && (tank.isEmpty() || sameFluid) && (!onlyBooze || isBooze(contained))) {
			try (Transaction tx = Transaction.openRoot()) {
				if (ResourceHandlerUtil.move(handler, tank, fluid -> true, Integer.MAX_VALUE, tx) > 0
						&& moveContainer(inSlot, outSlot, ItemUtil.getStack(holder, 0), tx)) {
					tx.commit();
					return true;
				}
			}
		}

		// fill the container from the tank
		if (!tank.isEmpty() && (contained.isEmpty() || sameFluid)) {
			try (Transaction tx = Transaction.openRoot()) {
				if (ResourceHandlerUtil.move(tank, handler, fluid -> true, Integer.MAX_VALUE, tx) > 0
						&& moveContainer(inSlot, outSlot, ItemUtil.getStack(holder, 0), tx)) {
					tx.commit();
					return true;
				}
			}
		}
		return false;
	}

	private static FluidStack contents(ResourceHandler<FluidResource> handler) {
		for (int index = 0; index < handler.size(); index++) {
			FluidStack fluid = FluidUtil.getStack(handler, index);
			if (!fluid.isEmpty()) return fluid;
		}
		return FluidStack.EMPTY;
	}

	/** Takes one container out of the input slot and puts the resulting container into the output slot. */
	private boolean moveContainer(int inSlot, int outSlot, ItemStack result, TransactionContext tx) {
		ItemResource in = items.getResource(inSlot);
		if (in.isEmpty() || items.extract(inSlot, in, 1, tx) != 1) return false;
		return result.isEmpty() || items.insert(outSlot, ItemResource.of(result), result.getCount(), tx) == result.getCount();
	}

	private boolean matches(ServerLevel level, RecipeHolder<BrewingRecipe> holder) {
		return holder.value().matches(new FluidRecipeInput(input.getFluid()), level) && holder.value().matchesCulture(culture.getFluid());
	}

	@Nullable
	private RecipeHolder<BrewingRecipe> findRecipe(ServerLevel level) {
		if (input.isEmpty()) return null;
		return level.recipeAccess().recipeMap().getRecipesFor(ModRecipes.BREWING.get(), new FluidRecipeInput(input.getFluid()), level)
				.filter(holder -> holder.value().matchesCulture(culture.getFluid()))
				.filter(holder -> output.isEmpty() || holder.value().getResultFluid().getFluid() == output.getFluidType() && output.getSpace() > 0)
				.findFirst().orElse(null);
	}

	private void brew(ServerLevel level, RecipeHolder<BrewingRecipe> holder) {
		int amount = Math.min(output.getSpace(), input.getFluidAmount());
		if (amount <= 0 || !matches(level, holder)) return;
		FluidStack result = holder.value().brew(culture.getFluid(), amount, level.getRandom());
		if (!output.isEmpty() && output.getFluidType() == result.getFluid()) {
			// joins the brew already in the output tank
			result = output.getFluid().copyWithAmount(amount);
		}
		if (output.fill(result, true) == amount && input.drain(amount, true).getAmount() == amount) {
			output.fill(result, false);
			input.drain(amount, false);
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
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
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putChild("Items", items);
		output.putChild("InputTank", input);
		output.putChild("OutputTank", this.output);
		output.putChild("CultureTank", culture);
		output.putInt("BrewTime", brewTime);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.readChild("Items", items);
		input.readChild("InputTank", this.input);
		input.readChild("OutputTank", output);
		input.readChild("CultureTank", culture);
		brewTime = input.getIntOr("BrewTime", 0);
	}
}
