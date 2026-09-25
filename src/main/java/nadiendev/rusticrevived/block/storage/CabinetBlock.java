package nadiendev.rusticrevived.block.storage;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.blockentity.storage.CabinetBlockEntity;
import nadiendev.rusticrevived.menu.storage.DoubleCabinetMenu;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Cabinet, single or double (legacy BlockCabinet). A cabinet placed on top of a single cabinet with
 * the same facing and mirror state becomes its {@link #TOP} half; the lower half ({@link #BOTTOM})
 * renders the whole double cabinet. Cabinets only open from the front and are fully drawn by the
 * block entity renderer (animated door, tinted by the planks they were made from).
 */
public class CabinetBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public static final MapCodec<CabinetBlock> CODEC = simpleCodec(CabinetBlock::new);
	/** Door hinge on the other side. */
	public static final BooleanProperty MIRROR = BooleanProperty.create("mirror");
	/** Upper half of a double cabinet. */
	public static final BooleanProperty TOP = BooleanProperty.create("top");
	/** Lower half of a double cabinet (legacy actual state, now stored). */
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

	public CabinetBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MIRROR, false).setValue(TOP, false)
				.setValue(BOTTOM, false));
	}

	@Override
	protected MapCodec<? extends CabinetBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, MIRROR, TOP, BOTTOM);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getHorizontalDirection().getOpposite();
		// the hit position is relative to the clicked block, as in 1.12
		BlockPos clicked = context.replacingClickedOnBlock() ? context.getClickedPos()
				: context.getClickedPos().relative(context.getClickedFace().getOpposite());
		Vec3 hit = context.getClickLocation().subtract(Vec3.atLowerCornerOf(clicked));
		boolean mirror = switch (facing) {
			case NORTH -> hit.x >= 0.5;
			case SOUTH -> hit.x < 0.5;
			case EAST -> hit.z >= 0.5;
			default -> hit.z < 0.5;
		};
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = defaultBlockState().setValue(FACING, facing).setValue(MIRROR, mirror);
		state = state.setValue(TOP, canBeTopOf(state, level.getBlockState(pos.below())));
		return state.setValue(BOTTOM, isBottomOf(state, level.getBlockState(pos.above())));
	}

	/** Whether {@code state} connects to the single cabinet {@code below} as the upper half. */
	private boolean canBeTopOf(BlockState state, BlockState below) {
		return below.is(this) && !below.getValue(TOP) && below.getValue(MIRROR) == state.getValue(MIRROR)
				&& below.getValue(FACING) == state.getValue(FACING);
	}

	private boolean isBottomOf(BlockState state, BlockState above) {
		return !state.getValue(TOP) && above.is(this) && above.getValue(TOP);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos,
			BlockPos neighborPos) {
		if (direction == Direction.DOWN && state.getValue(TOP) && !canBeTopOf(state, neighborState)) {
			return state.setValue(TOP, false);
		}
		if (direction == Direction.UP) {
			return state.setValue(BOTTOM, isBottomOf(state, neighborState));
		}
		return state;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (oldState.is(this) && oldState != state) {
			// joined or split: the exposed item handler changes between 27 and 54 slots
			level.invalidateCapabilities(pos);
		}
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE) return state;
		return rotate(state, mirror.getRotation(state.getValue(FACING))).setValue(MIRROR, !state.getValue(MIRROR));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CabinetBlockEntity(pos, state);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide && type == ModBlockEntities.CABINET.get()
				? (BlockEntityTicker<T>) (BlockEntityTicker<CabinetBlockEntity>) CabinetBlockEntity::lidAnimateTick : null;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet) {
			cabinet.recheckOpen();
		}
	}

	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		return blockEntity != null && blockEntity.triggerEvent(id, param);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (hitResult.getDirection() != state.getValue(FACING)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide) {
			MenuProvider provider = getMenuProvider(state, level, pos);
			if (provider != null) {
				player.openMenu(provider, buf -> buf.writeBlockPos(pos));
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Nullable
	@Override
	protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
		if (!(level.getBlockEntity(pos) instanceof CabinetBlockEntity cabinet)) return null;
		CabinetBlockEntity other = getOtherHalf(level, pos, state);
		if (other == null) return cabinet;
		return state.getValue(TOP) ? new DoubleCabinetProvider(cabinet, other) : new DoubleCabinetProvider(other, cabinet);
	}

	/** The inventory of the whole (single or double) cabinet: upper half first, like the menu. */
	public static Container getContainer(CabinetBlockEntity cabinet) {
		Level level = cabinet.getLevel();
		if (level == null) return cabinet;
		BlockState state = cabinet.getBlockState();
		CabinetBlockEntity other = getOtherHalf(level, cabinet.getBlockPos(), state);
		if (other == null) return cabinet;
		return state.getValue(TOP) ? new CompoundContainer(cabinet, other) : new CompoundContainer(other, cabinet);
	}

	@Nullable
	private static CabinetBlockEntity getOtherHalf(Level level, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof CabinetBlock)) return null;
		BlockPos otherPos = state.getValue(TOP) ? pos.below() : state.getValue(BOTTOM) ? pos.above() : null;
		return otherPos != null && level.getBlockEntity(otherPos) instanceof CabinetBlockEntity other ? other : null;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		Containers.dropContentsOnDestroy(state, newState, level, pos);
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		Item material = stack.get(ModDataComponents.CABINET_MATERIAL.get());
		if (material != null) {
			tooltip.add(material.getDescription().copy().withStyle(ChatFormatting.GRAY));
		}
	}

	/** Opens both halves of a double cabinet as one 54 slot inventory named after the lower half. */
	private record DoubleCabinetProvider(CabinetBlockEntity top, CabinetBlockEntity bottom) implements MenuProvider {
		@Override
		public Component getDisplayName() {
			return bottom.getDisplayName();
		}

		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
			if (!top.canOpen(player) || !bottom.canOpen(player)) return null;
			top.unpackLootTable(player);
			bottom.unpackLootTable(player);
			return new DoubleCabinetMenu(containerId, playerInventory, new CompoundContainer(top, bottom));
		}
	}
}
