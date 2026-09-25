package nadiendev.rusticrevived.effect;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;

/**
 * Server side behaviour of Rustic's effects (legacy EventHandlerPotions).
 */
@EventBusSubscriber(modid = RusticRevived.MODID)
public final class EffectEvents {

	private EffectEvents() {
	}

	/** Drinking water sobers you up a little. */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onWaterBottleUse(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntity();
		MobEffectInstance effect = entity.getEffect(ModEffects.TIPSY);
		if (effect == null || entity.level().isClientSide) return;
		ItemStack stack = event.getItem();
		if (!stack.is(Items.POTION)) return;
		PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		if (!contents.is(Potions.WATER)) return;
		RandomSource rand = entity.getRandom();
		int duration = effect.getDuration();
		int amplifier = effect.getAmplifier();
		if (rand.nextFloat() < 0.1F) {
			amplifier--;
		} else {
			duration -= rand.nextInt(800) + 200;
		}
		entity.removeEffect(ModEffects.TIPSY);
		if (amplifier >= 0 && duration > 0) {
			entity.addEffect(new MobEffectInstance(ModEffects.TIPSY, duration, amplifier, false, false));
		}
	}

	@SubscribeEvent
	public static void onJump(LivingEvent.LivingJumpEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.hasEffect(ModEffects.FULLMETAL)) {
			Vec3 motion = entity.getDeltaMovement();
			entity.setDeltaMovement(motion.x, 0, motion.z);
		}
	}

	@SubscribeEvent
	public static void onMount(EntityMountEvent event) {
		if (!event.isMounting()) return;
		if (event.getEntityMounting() instanceof LivingEntity top && event.getEntityBeingMounted() instanceof LivingEntity
				&& top.hasEffect(ModEffects.FULLMETAL)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onSleep(CanPlayerSleepEvent event) {
		if (event.getEntity().hasEffect(ModEffects.FULLMETAL)) {
			event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
		}
	}

	@SubscribeEvent
	public static void onFall(LivingFallEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.hasEffect(ModEffects.FULLMETAL) && event.getDistance() >= 0.6F && !entity.isInWater() && !entity.isInLava()) {
			entity.playSound(SoundEvents.ANVIL_LAND, 1.0F, 1.0F);
		}
		if (entity.hasEffect(ModEffects.FEATHER)) {
			event.setDistance(0);
		}
	}

	/** Fullmetal makes the entity invulnerable to anything but the void / creative-bypassing damage. */
	@SubscribeEvent
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		DamageSource source = event.getSource();
		if (event.getEntity().hasEffect(ModEffects.FULLMETAL) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			event.setCanceled(true);
		}
	}

	/** Full stomach, magic resistance and wither ward reduce their matching damage types. */
	@SubscribeEvent
	public static void onDamage(LivingDamageEvent.Pre event) {
		DamageSource source = event.getSource();
		LivingEntity entity = event.getEntity();
		if (source.is(DamageTypes.STARVE)) {
			MobEffectInstance effect = entity.getEffect(ModEffects.FULL);
			if (effect != null) {
				event.setNewDamage(Math.max(event.getNewDamage() - (effect.getAmplifier() + 1), 0));
			}
		} else if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) {
			MobEffectInstance effect = entity.getEffect(ModEffects.MAGIC_RESISTANCE);
			if (effect != null) {
				event.setNewDamage(event.getNewDamage() / (2F * (effect.getAmplifier() + 1F)));
			}
		} else if (source.is(DamageTypes.WITHER)) {
			MobEffectInstance effect = entity.getEffect(ModEffects.WITHER_WARD);
			if (effect != null) {
				event.setNewDamage(event.getNewDamage() / (2F * (effect.getAmplifier() + 1F)));
			}
		}
	}

	/** Undying: a totem of undying in a bottle. */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onDeath(LivingDeathEvent event) {
		if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
		if (!(event.getEntity() instanceof Player player)) return;
		MobEffectInstance effect = player.getEffect(ModEffects.UNDYING);
		if (effect == null) return;

		player.removeEffect(ModEffects.UNDYING);
		player.setHealth(1.0F);
		player.removeAllEffects();
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
		player.level().broadcastEntityEvent(player, (byte) 35);
		if (effect.getAmplifier() > 0) {
			player.addEffect(new MobEffectInstance(ModEffects.UNDYING, effect.getDuration(), effect.getAmplifier() - 1,
					effect.isAmbient(), effect.isVisible()));
		}
		event.setCanceled(true);
	}

	/**
	 * Fire Power: left clicking shoots fireballs. Called server side from the
	 * {@code FirePowerAttackPayload} sent by the client.
	 */
	public static void doFirePowerAttack(Player p) {
		Level level = p.level();
		if (level.isClientSide) return;
		MobEffectInstance effect = p.getEffect(ModEffects.FIRE_POWER);
		if (effect == null) return;
		if (level.getFluidState(BlockPos.containing(p.getEyePosition())).is(FluidTags.WATER)) return;

		double f = 0.005D;
		Vec3 look = p.getLookAngle();
		Vec3 accel = new Vec3(look.x * 40D + p.getRandom().nextGaussian() * f, look.y * 40D, look.z * 40D + p.getRandom().nextGaussian() * f).normalize();
		AbstractHurtingProjectile fireball;
		if (effect.getAmplifier() > 0) {
			fireball = new LargeFireball(level, p, accel, effect.getAmplifier());
		} else {
			fireball = new SmallFireball(level, p, accel);
		}
		fireball.setPos(p.getX() + look.x, p.getEyeY() + look.y, p.getZ() + look.z);
		Vec3 motion = p.getDeltaMovement();
		fireball.setDeltaMovement(fireball.getDeltaMovement().add(motion.x, p.onGround() ? 0 : motion.y, motion.z));
		if (!level.noCollision(fireball, fireball.getBoundingBox().inflate(0.001D))) return;

		level.playSound(null, p.getX() + look.x, p.getEyeY() + look.y, p.getZ() + look.z, SoundEvents.FIRECHARGE_USE,
				SoundSource.NEUTRAL, 1.0F, 0.4F / (p.getRandom().nextFloat() * 0.4F + 1.2F));
		level.addFreshEntity(fireball);
	}
}
