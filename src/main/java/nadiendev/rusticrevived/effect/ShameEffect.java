package nadiendev.rusticrevived.effect;

import nadiendev.rusticrevived.registry.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Shame: the entity is pelted with (visual) tomatoes.
 */
public class ShameEffect extends RusticEffect {
	public ShameEffect() {
		super(MobEffectCategory.HARMFUL, 16409650);
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		RandomSource rand = level.getRandom();
		ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, ModItems.TOMATO.get());
		Vec3 vel = entity.getDeltaMovement();
		float width = entity.getBbWidth();
		float height = entity.getBbHeight();
		int num = rand.nextInt(6) + 6;
		for (int i = 0; i < num; i++) {
			double x = entity.getX() + vel.x;
			double y = entity.getY() + height * rand.nextDouble() + vel.y;
			double z = entity.getZ() + vel.z;
			switch (rand.nextInt(4)) {
				case 3 -> {
					x += width * (rand.nextDouble() - 0.5);
					z -= width * 0.5;
				}
				case 2 -> {
					x += width * (rand.nextDouble() - 0.5);
					z += width * 0.5;
				}
				case 1 -> {
					z += width * (rand.nextDouble() - 0.5);
					x -= width * 0.5;
				}
				default -> {
					z += width * (rand.nextDouble() - 0.5);
					x += width * 0.5;
				}
			}
			level.sendParticles(particle, x, y, z, 0, vel.x, vel.y, vel.z, 1D);
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 5 == 0;
	}
}
