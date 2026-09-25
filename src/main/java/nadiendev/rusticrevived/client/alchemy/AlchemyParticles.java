package nadiendev.rusticrevived.client.alchemy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;

/**
 * Client only particle effects of the alchemy machines (legacy ClientProxy.spawnAlchemySmokeFX).
 * Only called from client side block entity ticks.
 */
public final class AlchemyParticles {
	private static final float COLOR_SCALE = 0.4F;
	private static final double SMOKE_SPEED = 0.125D;

	private AlchemyParticles() {
	}

	/** Short-lived smoke whose colour slowly cycles with the brewing progress. */
	public static void spawnSmoke(int brewTime, double x, double y, double z) {
		Particle smoke = Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.SMOKE, x, y, z, 0, SMOKE_SPEED, 0);
		if (smoke == null) return;
		float r = COLOR_SCALE * (Mth.sin(30 + brewTime / 16F) * 0.5F + 0.5F);
		float g = COLOR_SCALE * (Mth.sin(brewTime / 16F) * 0.5F + 0.5F);
		float b = COLOR_SCALE * (Mth.sin(60 + brewTime / 16F) * 0.5F + 0.5F);
		if (smoke instanceof SingleQuadParticle quad) {
			quad.setColor(r, g, b);
		}
		smoke.setLifetime(10);
	}
}
