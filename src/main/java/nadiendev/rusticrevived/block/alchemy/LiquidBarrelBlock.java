package nadiendev.rusticrevived.block.alchemy;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.alchemy.LiquidBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Liquid barrel (legacy BlockLiquidBarrel): a 16 bucket tank that keeps its content when broken
 * (loot table copies the fluid component) and slowly collects rain water.
 */
public class LiquidBarrelBlock extends Block implements EntityBlock {
	public static final MapCodec<LiquidBarrelBlock> CODEC = simpleCodec(LiquidBarrelBlock::new);
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);
	private static final int RAIN_AMOUNT = 50;

	public LiquidBarrelBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends LiquidBarrelBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LiquidBarrelBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof LiquidBarrelBlockEntity barrel && AlchemyInteractions.isFluidContainer(stack)) {
			return AlchemyInteractions.useFluidContainerConsuming(level, player, hand, barrel.getTank());
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
		if (precipitation == Biome.Precipitation.RAIN && level.getBlockEntity(pos) instanceof LiquidBarrelBlockEntity barrel) {
			barrel.getTank().fill(new FluidStack(Fluids.WATER, RAIN_AMOUNT), false);
		}
	}
}
