package nadiendev.rusticrevived.blockentity.alchemy;

import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * Crushing tub (legacy TileEntityCrushingTub): holds one stack of items that are crushed into
 * its tank, one at a time, when an entity lands on the tub.
 */
public class CrushingTubBlockEntity extends SyncedBlockEntity {
	public static final int CAPACITY = 8000;

	private final AlchemyItems items = new AlchemyItems(1, slot -> sync());
	private final AlchemyTank tank = new AlchemyTank(CAPACITY, this::sync);
	/** Automation can only take fluid out of the tub. */
	private final ResourceHandler<FluidResource> drainOnly = new DrainOnlyFluidHandler(tank);

	public CrushingTubBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CRUSHING_TUB.get(), pos, state);
	}

	public AlchemyItems getItems() {
		return items;
	}

	public AlchemyTank getTank() {
		return tank;
	}

	public ResourceHandler<FluidResource> getFluidHandler() {
		return drainOnly;
	}

	/** Crushes one item of the held stack if it has a recipe and the tank has room for its fluid (server side). */
	public void crush() {
		ItemStack stack = items.getStackInSlot(0);
		if (stack.isEmpty() || !(level instanceof ServerLevel serverLevel)) return;
		serverLevel.recipeAccess().getRecipeFor(ModRecipes.CRUSHING_TUB.get(), new SingleRecipeInput(stack), serverLevel).ifPresent(holder -> {
			CrushingTubRecipe recipe = holder.value();
			FluidStack result = recipe.getResultFluid();
			if (tank.fill(result, true) != result.getAmount()) return;
			spawnCrushParticles(serverLevel, stack);
			tank.fill(result, false);
			items.extractItem(0, 1, false);
			ItemStack byproduct = recipe.getByproduct();
			if (!byproduct.isEmpty()) {
				Block.popResource(serverLevel, worldPosition, byproduct);
			}
			serverLevel.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_FALL, SoundSource.BLOCKS, 0.5F,
					serverLevel.getRandom().nextFloat() * 0.1F + 0.9F);
		});
	}

	/** Item crumbs flying out of the tub (one particle per packet to keep their individual speeds). */
	private void spawnCrushParticles(ServerLevel serverLevel, ItemStack stack) {
		RandomSource rand = serverLevel.getRandom();
		ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1)));
		int count = rand.nextInt(8) + 8;
		for (int i = 0; i < count; ++i) {
			serverLevel.sendParticles(particle, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, 0,
					rand.nextDouble() * 0.2D - 0.1D, rand.nextDouble() * 0.1D + 0.05D, rand.nextDouble() * 0.2D - 0.1D, 1.0D);
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		dropContents(items);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putChild("Items", items);
		output.putChild("Tank", tank);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.readChild("Items", items);
		input.readChild("Tank", tank);
	}
}
