package nadiendev.rusticrevived.block.decor;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Iron, golden or silver lantern (legacy BlockLantern). It can be placed against any face of any block:
 * standing, hanging ({@code facing=down}) or mounted on a wall.
 */
public class RusticLanternBlock extends Block {
	public static final MapCodec<RusticLanternBlock> CODEC = simpleCodec(RusticLanternBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	private static final VoxelShape SHAPE = Shapes.box(0.25, 0.0, 0.25, 0.75, 0.625, 0.75);
	private static final VoxelShape HANGING_SHAPE = Shapes.box(0.25, 0.375, 0.25, 0.75, 1.0, 0.75);

	public RusticLanternBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected MapCodec<? extends RusticLanternBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(FACING) == Direction.DOWN ? HANGING_SHAPE : SHAPE;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		double y = pos.getY() + (state.getValue(FACING) == Direction.DOWN ? 0.705 : 0.33);
		level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5, y, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
