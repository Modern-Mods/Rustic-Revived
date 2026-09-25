package nadiendev.rusticrevived.effect;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Fullmetal: the entity turns to iron. It can't move, jump, fly or ride, sinks like a rock and is
 * immune to damage (handled in {@link EffectEvents}).
 */
public class FullmetalEffect extends RusticEffect {
	public FullmetalEffect() {
		super(MobEffectCategory.BENEFICIAL, 8220521);
		addAttributeModifier(Attributes.MOVEMENT_SPEED, RusticRevived.id("effect.fullmetal.movement_speed"), -1D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		addAttributeModifier(NeoForgeMod.SWIM_SPEED, RusticRevived.id("effect.fullmetal.swim_speed"), -1D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		addAttributeModifier(Attributes.FLYING_SPEED, RusticRevived.id("effect.fullmetal.flying_speed"), -1D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		addAttributeModifier(Attributes.ATTACK_SPEED, RusticRevived.id("effect.fullmetal.attack_speed"), -0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, RusticRevived.id("effect.fullmetal.knockback_resistance"), 9001D, AttributeModifier.Operation.ADD_VALUE);
		addAttributeModifier(Attributes.JUMP_STRENGTH, RusticRevived.id("effect.fullmetal.jump_strength"), -1D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		entity.setJumping(false);
		entity.setSprinting(false);
		if (entity.getVehicle() instanceof LivingEntity) {
			entity.stopRiding();
		}
		if (!entity.onGround() && !entity.isNoGravity()) {
			Vec3 motion = entity.getDeltaMovement();
			double drop;
			if (entity.isInWater() || entity.isInLava()) {
				drop = 0.27D / 4D;
			} else if (entity.isFallFlying()) {
				drop = 0.32D / 4D;
			} else {
				drop = 0.07D;
			}
			entity.setDeltaMovement(motion.x, motion.y - drop, motion.z);
			entity.hurtMarked = true;
		}
		if (entity instanceof Player player && player.getAbilities().flying) {
			player.getAbilities().flying = false;
			player.onUpdateAbilities();
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}
}
