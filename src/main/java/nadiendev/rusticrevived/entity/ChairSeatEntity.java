package nadiendev.rusticrevived.entity;

import nadiendev.rusticrevived.block.decor.ChairBlock;
import nadiendev.rusticrevived.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Invisible entity players ride while sitting on a chair (legacy BlockChair.EntityChair). It sits on the seat of
 * the chair, turns its rider's body towards the chair's front and disappears as soon as nobody sits on it or the
 * chair is gone. Dismounting (sneaking) is handled by vanilla.
 */
public class ChairSeatEntity extends Entity {
	/** Height of the chair seat surface. */
	private static final double SEAT_HEIGHT = 0.625;
	/** How far the seat is moved towards the chair back. */
	private static final double BACK_OFFSET = 0.125;
	/** How far the rider may turn their head away from the chair's front. */
	private static final float MAX_TURN = 105.0F;

	public ChairSeatEntity(EntityType<? extends ChairSeatEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	/** Seat for the chair at {@code pos} whose front faces {@code facing}. */
	public ChairSeatEntity(Level level, BlockPos pos, Direction facing) {
		this(ModEntities.CHAIR.get(), level);
		setPos(pos.getX() + 0.5 - facing.getStepX() * BACK_OFFSET, pos.getY() + SEAT_HEIGHT, pos.getZ() + 0.5 - facing.getStepZ() * BACK_OFFSET);
		setYRot(facing.toYRot());
		yRotO = getYRot();
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			return;
		}
		if (!(level().getBlockState(blockPosition()).getBlock() instanceof ChairBlock) || getPassengers().isEmpty()) {
			discard();
			return;
		}
		for (Entity passenger : getPassengers()) {
			if (passenger.distanceToSqr(this) >= 1.0) {
				discard();
				return;
			}
		}
	}

	@Override
	protected void positionRider(Entity passenger, MoveFunction callback) {
		super.positionRider(passenger, callback);
		clampRotation(passenger);
	}

	@Override
	public void onPassengerTurned(Entity passenger) {
		clampRotation(passenger);
	}

	/** Turns the rider's body towards the chair's front and limits how far they can look away from it. */
	private void clampRotation(Entity passenger) {
		passenger.setYBodyRot(getYRot());
		float turn = Mth.wrapDegrees(passenger.getYRot() - getYRot());
		float clamped = Mth.clamp(turn, -MAX_TURN, MAX_TURN);
		passenger.yRotO += clamped - turn;
		passenger.setYRot(passenger.getYRot() + clamped - turn);
		passenger.setYHeadRot(passenger.getYRot());
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		Direction front = Direction.fromYRot(getYRot());
		for (Direction side : new Direction[] { front, front.getClockWise(), front.getCounterClockWise(), front.getOpposite() }) {
			Vec3 location = DismountHelper.findSafeDismountLocation(passenger.getType(), level(), blockPosition().relative(side), true);
			if (location != null) {
				return location;
			}
		}
		return super.getDismountLocationForPassenger(passenger);
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	/** Nothing to save: the seat is recreated every time a player sits down. */
	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}
}
