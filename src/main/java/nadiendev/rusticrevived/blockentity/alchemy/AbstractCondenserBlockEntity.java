package nadiendev.rusticrevived.blockentity.alchemy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.block.alchemy.AbstractCondenserBlock;
import nadiendev.rusticrevived.client.alchemy.AlchemyParticles;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.CondenserRecipeInput;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * Shared logic of the basic and advanced alchemic condensers (legacy TileEntityCondenserBase):
 * a furnace-fuelled machine turning ingredients, water and glass bottles into elixirs while
 * its retorts are attached.
 * <p>
 * Slots: result, fuel, bottle, then the ingredient slots (the advanced condenser puts its
 * modifier slot first). Automation is sided: bottom = result, back = fuel + bottle, sides and
 * front = ingredients, top = modifier (advanced only).
 */
public abstract class AbstractCondenserBlockEntity extends SyncedBlockEntity implements MenuProvider {
	public static final int SLOT_RESULT = 0;
	public static final int SLOT_FUEL = 1;
	public static final int SLOT_BOTTLE = 2;
	public static final int SLOT_INGREDIENTS_START = 3;
	public static final int CAPACITY = 8000;

	public static final int DATA_BURN_TIME = 0;
	public static final int DATA_ITEM_BURN_TIME = 1;
	public static final int DATA_BREW_TIME = 2;
	public static final int DATA_TOTAL_BREW_TIME = 3;
	public static final int DATA_COUNT = 4;

	protected final ItemStackHandler items;
	protected final FluidTank tank;
	private final IItemHandlerModifiable automation;
	private final IItemHandler resultAccess;
	private final IItemHandler backAccess;
	private final IItemHandler sideAccess;
	private final IItemHandler topAccess;

	@Nullable
	private RecipeHolder<CondenserRecipe> currentRecipe;
	private boolean contentChanged = true;
	/** Whether the condenser is currently brewing; synced to the client for the smoke particles. */
	private boolean brewing;
	private int burnTime;
	private int itemBurnTime;
	private int brewTime;
	private int totalBrewTime;

	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case DATA_BURN_TIME -> burnTime;
				case DATA_ITEM_BURN_TIME -> itemBurnTime;
				case DATA_BREW_TIME -> brewTime;
				case DATA_TOTAL_BREW_TIME -> totalBrewTime;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case DATA_BURN_TIME -> burnTime = value;
				case DATA_ITEM_BURN_TIME -> itemBurnTime = value;
				case DATA_BREW_TIME -> brewTime = value;
				case DATA_TOTAL_BREW_TIME -> totalBrewTime = value;
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return DATA_COUNT;
		}
	};

	protected AbstractCondenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
		super(type, pos, state);
		this.items = new ItemStackHandler(slots) {
			@Override
			protected void onContentsChanged(int slot) {
				contentChanged = true;
				setChanged();
			}
		};
		this.tank = new FluidTank(CAPACITY) {
			@Override
			protected void onContentsChanged() {
				contentChanged = true;
				sync();
			}
		};
		this.automation = new AutomationHandler();
		this.resultAccess = new RangedWrapper(automation, SLOT_RESULT, SLOT_RESULT + 1);
		this.backAccess = new RangedWrapper(automation, SLOT_FUEL, SLOT_BOTTLE + 1);
		this.sideAccess = new RangedWrapper(automation, firstIngredientSlot(), slots);
		this.topAccess = modifierSlot() >= 0 ? new RangedWrapper(automation, modifierSlot(), modifierSlot() + 1) : automation;
	}

	/** Index of the first ingredient slot. */
	protected abstract int firstIngredientSlot();

	/** Index of the modifier slot, or -1 when the condenser has none. */
	protected abstract int modifierSlot();

	/** Whether this is an advanced condenser (it accepts advanced recipes). */
	public abstract boolean isAdvanced();

	// ---------------------------------------------------------------- accessors

	public ItemStackHandler getItems() {
		return items;
	}

	public FluidTank getTank() {
		return tank;
	}

	public ContainerData getData() {
		return data;
	}

	/** Item handler exposed to automation on the given side (null = unsided). */
	public IItemHandler getItemHandler(@Nullable Direction side) {
		if (side == null) return automation;
		if (side == Direction.DOWN) return resultAccess;
		if (side == Direction.UP) return topAccess;
		if (side == getBlockState().getValue(AbstractCondenserBlock.FACING).getOpposite()) return backAccess;
		return sideAccess;
	}

	public static boolean isFuel(ItemStack stack) {
		return stack.getBurnTime(RecipeType.SMELTING) > 0;
	}

	// ---------------------------------------------------------------- ticking

	public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractCondenserBlockEntity condenser) {
		condenser.tickServer();
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, AbstractCondenserBlockEntity condenser) {
		if (condenser.brewing) {
			condenser.brewTime++;
			condenser.spawnSmoke();
		}
	}

	private void tickServer() {
		boolean wasBrewing = brewing;
		boolean canBrew = canBrew();
		boolean hasRecipe = canBrew && hasValidRecipe();
		if (burnFuel(hasRecipe) && hasRecipe) {
			brewing = true;
			brewTime++;
			if (brewTime >= totalBrewTime) {
				brewTime = 0;
				brew();
			}
		} else {
			brewing = false;
			if (brewTime > 0) {
				brewTime = Mth.clamp(brewTime - 2, 0, totalBrewTime);
			}
		}
		if (brewing != wasBrewing) {
			sync();
		} else if (burnTime > 0 || brewTime > 0) {
			setChanged();
		}
	}

	private boolean canBrew() {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof AbstractCondenserBlock block) || !state.getValue(AbstractCondenserBlock.BOTTOM)) {
			return false;
		}
		boolean hasIngredient = false;
		for (int slot = firstIngredientSlot(); slot < items.getSlots(); slot++) {
			hasIngredient |= !items.getStackInSlot(slot).isEmpty();
		}
		return hasIngredient && level != null && block.hasRetorts(level, worldPosition, state);
	}

	private boolean hasValidRecipe() {
		// short-cut the recipe matching when there is no fuel at all
		if (burnTime <= 0 && items.getStackInSlot(SLOT_FUEL).isEmpty()) {
			return false;
		}
		refreshRecipe();
		if (currentRecipe == null) {
			return false;
		}
		CondenserRecipe recipe = currentRecipe.value();
		if (tank.getFluidAmount() < recipe.fluid().amount()) {
			return false;
		}
		return items.insertItem(SLOT_RESULT, recipe.assemble(createInput(), level.registryAccess()), true).isEmpty();
	}

	private void refreshRecipe() {
		if (!contentChanged || level == null) return;
		contentChanged = false;
		CondenserRecipeInput input = createInput();
		if (currentRecipe != null && currentRecipe.value().matches(input, level)) {
			return;
		}
		brewTime = 0;
		currentRecipe = level.getRecipeManager().getRecipeFor(ModRecipes.CONDENSER.get(), input, level).orElse(null);
		if (currentRecipe != null) {
			totalBrewTime = currentRecipe.value().getTime();
		}
	}

	private CondenserRecipeInput createInput() {
		List<ItemStack> ingredients = new ArrayList<>();
		for (int slot = firstIngredientSlot(); slot < items.getSlots(); slot++) {
			ingredients.add(items.getStackInSlot(slot));
		}
		ItemStack modifier = modifierSlot() >= 0 ? items.getStackInSlot(modifierSlot()) : ItemStack.EMPTY;
		return new CondenserRecipeInput(ingredients, modifier, items.getStackInSlot(SLOT_BOTTLE), tank.getFluid(), isAdvanced());
	}

	/**
	 * Burns one tick of fuel, lighting a new fuel item if needed and allowed.
	 *
	 * @return whether the condenser is burning
	 */
	private boolean burnFuel(boolean consumeNewFuel) {
		if (burnTime > 0) {
			burnTime--;
			return true;
		}
		ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
		if (consumeNewFuel && !fuel.isEmpty()) {
			burnTime = fuel.getBurnTime(RecipeType.SMELTING);
			itemBurnTime = burnTime;
			ItemStack remainder = fuel.getCraftingRemainingItem();
			items.extractItem(SLOT_FUEL, 1, false);
			if (items.getStackInSlot(SLOT_FUEL).isEmpty() && !remainder.isEmpty()) {
				items.setStackInSlot(SLOT_FUEL, remainder);
			}
			return burnTime > 0;
		}
		return false;
	}

	private void brew() {
		if (currentRecipe == null || level == null || !canBrew()) return;
		CondenserRecipe recipe = currentRecipe.value();
		CondenserRecipeInput input = createInput();
		if (!recipe.matches(input, level) || tank.getFluidAmount() < recipe.fluid().amount()) return;
		items.insertItem(SLOT_RESULT, recipe.assemble(input, level.registryAccess()), false);
		for (int slot = firstIngredientSlot(); slot < items.getSlots(); slot++) {
			items.extractItem(slot, 1, false);
		}
		if (modifierSlot() >= 0) {
			items.extractItem(modifierSlot(), 1, false);
		}
		items.extractItem(SLOT_BOTTLE, 1, false);
		tank.drain(recipe.fluid().amount(), IFluidHandler.FluidAction.EXECUTE);
	}

	/** Coloured smoke rising from every attached retort (legacy spawnAlchemySmokeFX). */
	private void spawnSmoke() {
		BlockState state = getBlockState();
		if (level == null || !(state.getBlock() instanceof AbstractCondenserBlock block)) return;
		for (Direction side : block.retortSides(state.getValue(AbstractCondenserBlock.FACING))) {
			AlchemyParticles.spawnSmoke(brewTime, worldPosition.getX() + 0.5D + side.getStepX(), worldPosition.getY() + 1.0625D,
					worldPosition.getZ() + 0.5D + side.getStepZ());
		}
	}

	/** Drops the machine inventory (the block was broken). */
	public void dropContents() {
		dropContents(items);
	}

	// ---------------------------------------------------------------- persistence

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("Items", items.serializeNBT(registries));
		tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
		tag.putInt("BurnTime", burnTime);
		tag.putInt("ItemBurnTime", itemBurnTime);
		tag.putInt("BrewTime", brewTime);
		tag.putInt("BrewTimeTotal", totalBrewTime);
		tag.putBoolean("Brewing", brewing);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		items.deserializeNBT(registries, tag.getCompound("Items"));
		tank.readFromNBT(registries, tag.getCompound("Tank"));
		burnTime = tag.getInt("BurnTime");
		itemBurnTime = tag.getInt("ItemBurnTime");
		brewTime = tag.getInt("BrewTime");
		totalBrewTime = tag.getInt("BrewTimeTotal");
		brewing = tag.getBoolean("Brewing");
		contentChanged = true;
	}

	/**
	 * The machine inventory as seen by automation: nothing goes into the result slot, only fuel
	 * goes into the fuel slot and fuel never goes into the bottle slot.
	 */
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
			return switch (slot) {
				case SLOT_RESULT -> false;
				case SLOT_FUEL -> isFuel(stack);
				case SLOT_BOTTLE -> !isFuel(stack);
				default -> true;
			};
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			items.setStackInSlot(slot, stack);
		}
	}
}
