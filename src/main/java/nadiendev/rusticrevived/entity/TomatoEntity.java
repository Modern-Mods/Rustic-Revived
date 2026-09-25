package nadiendev.rusticrevived.entity;

import nadiendev.rusticrevived.registry.ModEffects;
import nadiendev.rusticrevived.registry.ModEntities;
import nadiendev.rusticrevived.registry.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A thrown tomato. Splats on impact and puts the target to shame.
 */
public class TomatoEntity extends ThrowableItemProjectile {

	public TomatoEntity(EntityType<? extends TomatoEntity> type, Level level) {
		super(type, level);
	}

	public TomatoEntity(Level level, LivingEntity shooter) {
		super(ModEntities.TOMATO.get(), shooter, level);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.TOMATO.get();
	}

	private ParticleOptions getParticle() {
		return new ItemParticleOption(ParticleTypes.ITEM, getItem());
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			ParticleOptions particle = getParticle();
			for (int i = 0; i < 8; ++i) {
				level().addParticle(particle, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (result.getEntity() == getOwner()) return;
		result.getEntity().hurt(damageSources().thrown(this, getOwner()), 0.0F);
		if (!level().isClientSide && result.getEntity() instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(ModEffects.SHAME, 400, 0, false, false));
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		level().playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_HURT, SoundSource.NEUTRAL, 0.5F,
				0.4F / (level().getRandom().nextFloat() * 0.4F + 0.8F));
		if (!level().isClientSide) {
			level().broadcastEntityEvent(this, (byte) 3);
			discard();
		}
	}
}
