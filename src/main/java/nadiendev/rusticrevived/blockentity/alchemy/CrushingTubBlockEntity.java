package nadiendev.rusticrevived.blockentity.alchemy;

import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Crushing tub (legacy TileEntityCrushingTub): holds one stack of items that are crushed into
 * its tank, one at a time, when an entity lands on the tub.
 */
public class CrushingTubBlockEntity extends SyncedBlockEntity {
	public static final int CAPACITY = 8000;

	private final ItemStackHandler items = new ItemStackHandler(1) {
		@Override
		protected void onContentsChanged(int slot) {
			sync();
		}
	};
	private final FluidTank tank = new FluidTank(CAPACITY) {
		@Override
		protected void onContentsChanged() {
			sync();
		}
	};
	/** Automation can only take fluid out of the tub. */
	private final IFluidHandler drainOnly = new DrainOnlyFluidHandler(tank);

	public CrushingTubBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CRUSHING_TUB.get(), pos, state);
	}

	public ItemStackHandler getItems() {
		return items;
	}

	public FluidTank getTank() {
		return tank;
	}

	public IFluidHandler getFluidHandler() {
		return drainOnly;
	}

	/** Crushes one item of the held stack if it has a recipe and the tank has room for its fluid. */
	public void crush() {
		ItemStack stack = items.getStackInSlot(0);
		if (stack.isEmpty() || level == null) return;
		level.getRecipeManager().getRecipeFor(ModRecipes.CRUSHING_TUB.get(), new SingleRecipeInput(stack), level).ifPresent(holder -> {
			CrushingTubRecipe recipe = holder.value();
			FluidStack result = recipe.getResultFluid();
			if (tank.fill(result, IFluidHandler.FluidAction.SIMULATE) != result.getAmount()) return;
			if (level.isClientSide) {
				spawnCrushParticles(stack);
				return;
			}
			tank.fill(result, IFluidHandler.FluidAction.EXECUTE);
			items.extractItem(0, 1, false);
			ItemStack byproduct = recipe.getByproduct();
			if (!byproduct.isEmpty()) {
				Block.popResource(level, worldPosition, byproduct);
			}
			level.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
		});
	}

	private void spawnCrushParticles(ItemStack stack) {
		RandomSource rand = level.random;
		ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, stack);
		int count = rand.nextInt(8) + 8;
		for (int i = 0; i < count; ++i) {
			level.addParticle(particle, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D,
					rand.nextDouble() * 0.2D - 0.1D, rand.nextDouble() * 0.1D + 0.05D, rand.nextDouble() * 0.2D - 0.1D);
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
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		items.deserializeNBT(registries, tag.getCompound("Items"));
		tank.readFromNBT(registries, tag.getCompound("Tank"));
	}
}
