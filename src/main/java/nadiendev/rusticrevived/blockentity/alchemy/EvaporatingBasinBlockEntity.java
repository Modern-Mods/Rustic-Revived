package nadiendev.rusticrevived.blockentity.alchemy;

import java.util.Optional;

import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import nadiendev.rusticrevived.recipe.FluidRecipeInput;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Drying basin (legacy TileEntityEvaporatingBasin): the fluid slowly evaporates (the recipe
 * amount per recipe time, 1 mB/tick by default) and once enough of it has evaporated the recipe
 * result appears in the basin.
 */
public class EvaporatingBasinBlockEntity extends SyncedBlockEntity {
	public static final int CAPACITY = 6000;
	private static final int UPDATE_INTERVAL = 20;

	private final ItemStackHandler items = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			sync();
		}
	};
	/** Automation can only take the dried result out. */
	private final IItemHandler extractOnly = new IItemHandler() {
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
			return stack;
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
			return false;
		}
	};
	private final FluidTank tank = new FluidTank(CAPACITY, fluid -> findRecipe(fluid).isPresent()) {
		@Override
		protected void onContentsChanged() {
			sync();
		}
	};

	/** Fluid that already evaporated but did not form a result yet. */
	private FluidStack evaporated = FluidStack.EMPTY;
	/** Rounding remainder of the evaporation rate. */
	private int remainder;

	public EvaporatingBasinBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.EVAPORATING_BASIN.get(), pos, state);
	}

	public ItemStackHandler getItems() {
		return items;
	}

	public IItemHandler getAutomationHandler() {
		return extractOnly;
	}

	public FluidTank getTank() {
		return tank;
	}

	private Optional<RecipeHolder<EvaporatingBasinRecipe>> findRecipe(FluidStack fluid) {
		if (level == null || fluid.isEmpty()) return Optional.empty();
		return level.getRecipeManager().getRecipeFor(ModRecipes.EVAPORATING_BASIN.get(), new FluidRecipeInput(fluid), level);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, EvaporatingBasinBlockEntity basin) {
		if (level.getGameTime() % UPDATE_INTERVAL == 0) {
			basin.evaporate();
		}
	}

	private void evaporate() {
		if (tank.isEmpty() || !evaporated.isEmpty() && !FluidStack.isSameFluid(evaporated, tank.getFluid())) {
			// the fluid changed: start over
			evaporated = FluidStack.EMPTY;
			remainder = 0;
		}
		findRecipe(tank.getFluid()).ifPresent(holder -> {
			EvaporatingBasinRecipe recipe = holder.value();
			int progress = UPDATE_INTERVAL * recipe.getAmount() + remainder;
			remainder = progress % recipe.getTime();
			FluidStack drained = tank.drain(progress / recipe.getTime(), IFluidHandler.FluidAction.EXECUTE);
			if (evaporated.isEmpty()) {
				evaporated = drained;
			} else {
				evaporated.grow(drained.getAmount());
			}
			setChanged();
		});
		if (!evaporated.isEmpty()) {
			findRecipe(evaporated).ifPresent(holder -> {
				EvaporatingBasinRecipe recipe = holder.value();
				ItemStack result = recipe.assemble(new FluidRecipeInput(evaporated), level.registryAccess());
				if (evaporated.getAmount() >= recipe.getAmount() && items.insertItem(0, result, true).isEmpty()) {
					evaporated.shrink(recipe.getAmount());
					items.insertItem(0, result, false);
				}
			});
		}
	}

	public void dropContents() {
		dropContents(items);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("Items", items.serializeNBT(registries));
		tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
		if (!evaporated.isEmpty()) {
			tag.put("EvaporatedFluid", evaporated.save(registries));
		}
		tag.putInt("Remainder", remainder);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		items.deserializeNBT(registries, tag.getCompound("Items"));
		tank.readFromNBT(registries, tag.getCompound("Tank"));
		evaporated = FluidStack.parseOptional(registries, tag.getCompound("EvaporatedFluid"));
		remainder = tag.getInt("Remainder");
	}
}
