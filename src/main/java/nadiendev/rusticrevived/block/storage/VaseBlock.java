package nadiendev.rusticrevived.block.storage;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.storage.VaseBlockEntity;
import nadiendev.rusticrevived.item.VaseItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Pot with 27 slots and several designs (legacy BlockVase). The design is a block state property
 * that the item carries in its {@code block_state} component.
 */
public class VaseBlock extends Block implements EntityBlock {
	public static final MapCodec<VaseBlock> CODEC = simpleCodec(VaseBlock::new);
	public static final int MIN_DESIGN = 0;
	public static final int MAX_DESIGN = 12;
	/** Design of the vase (legacy "variant" metadata); designs 8+ use the alternative model. */
	public static final IntegerProperty DESIGN = IntegerProperty.create("variant", MIN_DESIGN, MAX_DESIGN);
	/** Order in which scrolling cycles through the designs: grouped by colour, then by model. */
	public static final int[] DESIGN_ORDER = {
			0, // brown ceramic
			5, // brown ceramic with horse design
			6, // white ceramic
			2, // white ceramic with blue pattern
			1, // white ceramic with brown pattern
			7, // light gray ceramic
			4, // dark gray ceramic
			3, // green ceramic
			8, // alt model brown ceramic
			9, // alt model white ceramic
			10, // alt model white ceramic with blue pattern
			12, // alt model medium gray ceramic
			11, // alt model dark gray ceramic
	};
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

	public VaseBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(DESIGN, MIN_DESIGN));
	}

	@Override
	protected MapCodec<? extends VaseBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DESIGN);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VaseBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VaseBlockEntity vase) {
			player.openMenu(vase, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
		if (stack.getItem() instanceof VaseItem vase) {
			vase.setDesign(stack, state.getValue(DESIGN));
		}
		return stack;
	}
}
