package nadiendev.rusticrevived.blockentity.alchemy;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import nadiendev.rusticrevived.block.alchemy.AbstractCondenserBlock;
import nadiendev.rusticrevived.client.alchemy.AlchemyParticles;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.CondenserRecipeInput;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

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

	protected final AlchemyItems items;
	protected final AlchemyTank tank;
	private final ResourceHandler<ItemResource> automation;
	private final ResourceHandler<ItemResource> resultAccess;
	private final ResourceHandler<ItemResource> backAccess;
	private final ResourceHandler<ItemResource> sideAccess;
	private final ResourceHandler<ItemResource> topAccess;

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
		this.items = new AlchemyItems(slots, slot -> {
			contentChanged = true;
			setChanged();
		});
		this.tank = new AlchemyTank(CAPACITY, () -> {
			contentChanged = true;
			sync();
		});
		this.automation = new AutomationItemHandler(items, (slot, stack) -> switch (slot) {
			case SLOT_RESULT -> false;
			case SLOT_FUEL -> level != null && isFuel(stack, level.fuelValues());
			case SLOT_BOTTLE -> level == null || !isFuel(stack, level.fuelValues());
			default -> true;
		});
		this.resultAccess = RangedResourceHandler.of(automation, SLOT_RESULT, SLOT_RESULT + 1);
		this.backAccess = RangedResourceHandler.of(automation, SLOT_FUEL, SLOT_BOTTLE + 1);
		this.sideAccess = RangedResourceHandler.of(automation, firstIngredientSlot(), slots);
		this.topAccess = modifierSlot() >= 0 ? RangedResourceHandler.ofSingleIndex(automation, modifierSlot()) : automation;
	}

	/** Index of the first ingredient slot. */
	protected abstract int firstIngredientSlot();

	/** Index of the modifier slot, or -1 when the condenser has none. */
	protected abstract int modifierSlot();

	/** Whether this is an advanced condenser (it accepts advanced recipes). */
	public abstract boolean isAdvanced();

	// ---------------------------------------------------------------- accessors

	public AlchemyItems getItems() {
		return items;
	}

	public AlchemyTank getTank() {
		return tank;
	}

	public ContainerData getData() {
		return data;
	}

	/** Item handler exposed to automation on the given side (null = unsided). */
	public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
		if (side == null) return automation;
		if (side == Direction.DOWN) return resultAccess;
		if (side == Direction.UP) return topAccess;
		if (side == getBlockState().getValue(AbstractCondenserBlock.FACING).getOpposite()) return backAccess;
		return sideAccess;
	}

	public static boolean isFuel(ItemStack stack, FuelValues fuelValues) {
		return stack.getBurnTime(RecipeType.SMELTING, fuelValues) > 0;
	}

	// ---------------------------------------------------------------- ticking

	public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractCondenserBlockEntity condenser) {
		condenser.tickServer((ServerLevel) level);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, AbstractCondenserBlockEntity condenser) {
		if (condenser.brewing) {
			condenser.brewTime++;
			condenser.spawnSmoke();
		}
	}

	private void tickServer(ServerLevel level) {
		boolean wasBrewing = brewing;
		boolean canBrew = canBrew();
		boolean hasRecipe = canBrew && hasValidRecipe(level);
		if (burnFuel(level, hasRecipe) && hasRecipe) {
			brewing = true;
			brewTime++;
			if (brewTime >= totalBrewTime) {
				brewTime = 0;
				brew(level);
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
		for (int slot = firstIngredientSlot(); slot < items.size(); slot++) {
			hasIngredient |= !items.getResource(slot).isEmpty();
		}
		return hasIngredient && level != null && block.hasRetorts(level, worldPosition, state);
	}

	private boolean hasValidRecipe(ServerLevel level) {
		// short-cut the recipe matching when there is no fuel at all
		if (burnTime <= 0 && items.getResource(SLOT_FUEL).isEmpty()) {
			return false;
		}
		refreshRecipe(level);
		if (currentRecipe == null) {
			return false;
		}
		CondenserRecipe recipe = currentRecipe.value();
		if (tank.getFluidAmount() < recipe.fluid().amount()) {
			return false;
		}
		return items.insertItem(SLOT_RESULT, recipe.assemble(createInput()), true).isEmpty();
	}

	private void refreshRecipe(ServerLevel level) {
		if (!contentChanged) return;
		contentChanged = false;
		CondenserRecipeInput input = createInput();
		if (currentRecipe != null && currentRecipe.value().matches(input, level)) {
			return;
		}
		brewTime = 0;
		currentRecipe = level.recipeAccess().getRecipeFor(ModRecipes.CONDENSER.get(), input, level).orElse(null);
		if (currentRecipe != null) {
			totalBrewTime = currentRecipe.value().getTime();
		}
	}

	private CondenserRecipeInput createInput() {
		List<ItemStack> ingredients = new ArrayList<>();
		for (int slot = firstIngredientSlot(); slot < items.size(); slot++) {
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
	private boolean burnFuel(ServerLevel level, boolean consumeNewFuel) {
		if (burnTime > 0) {
			burnTime--;
			return true;
		}
		ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
		if (consumeNewFuel && !fuel.isEmpty()) {
			burnTime = fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues());
			itemBurnTime = burnTime;
			ItemStackTemplate remainder = fuel.getCraftingRemainder();
			items.extractItem(SLOT_FUEL, 1, false);
			if (items.getResource(SLOT_FUEL).isEmpty() && remainder != null) {
				items.setStackInSlot(SLOT_FUEL, remainder.create());
			}
			return burnTime > 0;
		}
		return false;
	}

	private void brew(ServerLevel level) {
		if (currentRecipe == null || !canBrew()) return;
		CondenserRecipe recipe = currentRecipe.value();
		CondenserRecipeInput input = createInput();
		if (!recipe.matches(input, level) || tank.getFluidAmount() < recipe.fluid().amount()) return;
		items.insertItem(SLOT_RESULT, recipe.assemble(input), false);
		for (int slot = firstIngredientSlot(); slot < items.size(); slot++) {
			items.extractItem(slot, 1, false);
		}
		if (modifierSlot() >= 0) {
			items.extractItem(modifierSlot(), 1, false);
		}
		items.extractItem(SLOT_BOTTLE, 1, false);
		tank.drain(recipe.fluid().amount(), false);
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

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		// the machine inventory drops when the block is broken
		dropContents(items);
	}

	// ---------------------------------------------------------------- persistence

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putChild("Items", items);
		output.putChild("Tank", tank);
		output.putInt("BurnTime", burnTime);
		output.putInt("ItemBurnTime", itemBurnTime);
		output.putInt("BrewTime", brewTime);
		output.putInt("BrewTimeTotal", totalBrewTime);
		output.putBoolean("Brewing", brewing);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.readChild("Items", items);
		input.readChild("Tank", tank);
		burnTime = input.getIntOr("BurnTime", 0);
		itemBurnTime = input.getIntOr("ItemBurnTime", 0);
		brewTime = input.getIntOr("BrewTime", 0);
		totalBrewTime = input.getIntOr("BrewTimeTotal", 0);
		brewing = input.getBooleanOr("Brewing", false);
		contentChanged = true;
	}
}
