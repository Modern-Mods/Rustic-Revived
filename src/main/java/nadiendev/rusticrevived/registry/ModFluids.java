package nadiendev.rusticrevived.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.fluid.DrinkableFluidType;
import nadiendev.rusticrevived.fluid.RusticLiquidBlock;
import nadiendev.rusticrevived.util.RusticDamage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Rustic's juices, oils and alcoholic beverages.
 * <p>
 * Juices and oils ({@link #JUICES}) can be placed in the world and have buckets. Booze
 * ({@link #BOOZE}) only exists inside containers since its quality lives in a
 * {@link net.neoforged.neoforge.fluids.FluidStack} component.
 */
public final class ModFluids {
	public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, RusticRevived.NAMESPACE);
	public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, RusticRevived.NAMESPACE);

	public static final List<FluidEntry> ALL = new ArrayList<>();
	public static final List<FluidEntry> JUICES = new ArrayList<>();
	public static final List<FluidEntry> BOOZE = new ArrayList<>();

	// ---------------------------------------------------------------- juices & oils

	public static final FluidEntry OLIVE_OIL = juice("olive_oil", props(920, 2000), 2, (level, player, stack, fluid) -> {
		player.getFoodData().eat(1, 0.4F);
		player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 600, 1));
	}, null);

	public static final FluidEntry IRONBERRY_JUICE = juice("ironberry_juice", props(1100, 1100), 1, (level, player, stack, fluid) -> {
		player.getFoodData().eat(1, 0.8F);
		addFullmetal(player, 30 * 20);
	}, null);

	public static final FluidEntry WILDBERRY_JUICE = juice("wildberry_juice", props(1070, 1100), 1, (level, player, stack, fluid) -> {
		player.getFoodData().eat(1, 1F);
		if (player.getRandom().nextFloat() < 0.2F) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 75));
		}
		if (player.getRandom().nextFloat() < 0.1F) {
			player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
		}
	}, null);

	public static final FluidEntry GRAPE_JUICE = juice("grape_juice", props(1070, 1100), 1,
			(level, player, stack, fluid) -> player.getFoodData().eat(1, 0.9F), null);

	public static final FluidEntry APPLE_JUICE = juice("apple_juice", props(1050, 1100), 1,
			(level, player, stack, fluid) -> player.getFoodData().eat(1, 1.2F), null);

	public static final FluidEntry ALE_WORT = juice("ale_wort", props(1004, 2000), 2, (level, player, stack, fluid) -> {
		player.getFoodData().eat(1, 2F);
		player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 400, 1));
	}, null);

	public static final FluidEntry HONEY = juice("honey", props(1433, 5500), 4, (level, player, stack, fluid) -> {
		player.getFoodData().eat(3, 0.4F);
		if (player.getRandom().nextFloat() < 0.6F) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 75));
		}
	}, (state, level, pos, entity) -> entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D)));

	public static final FluidEntry GOLDEN_APPLE_JUICE = juice("golden_apple_juice", props(1050, 1100).rarity(Rarity.EPIC), 1, (level, player, stack, fluid) -> {
		player.getFoodData().eat(3, 1.8F);
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 6000, 0));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
	}, (state, level, pos, entity) -> {
		if (entity instanceof LivingEntity living && living.tickCount % 10 == 0) {
			living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, true));
		}
	});

	public static final FluidEntry VANTA_OIL = juice("vanta_oil", props(920, 2000), 2, (level, player, stack, fluid) -> {
		player.getFoodData().eat(1, 0.4F);
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600, 0));
	}, null);

	// ---------------------------------------------------------------- booze

	public static final FluidEntry ALE = booze("ale", 0.5F, props(1004, 1016), (level, player, quality) -> {
		if (quality >= 0.5F) {
			player.getFoodData().eat(2, 4F * quality);
			int duration = 1200 + (int) (10800 * Math.max(Math.abs((quality - 0.5F) * 2F), 0F));
			player.addEffect(new MobEffectInstance(ModEffects.FULL, duration));
		} else {
			int duration = (int) (6000 * Math.max(1 - quality, 0.25));
			player.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration));
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, duration));
		}
	});

	public static final FluidEntry CIDER = booze("cider", 0.5F, props(1004, 1400), (level, player, quality) -> {
		if (quality >= 0.5F) {
			player.getFoodData().eat(1, 2F * quality);
			int duration = 1200 + (int) (10800 * Math.max(Math.abs((quality - 0.5F) * 2F), 0F));
			player.addEffect(new MobEffectInstance(ModEffects.MAGIC_RESISTANCE, duration));
		} else {
			player.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (1200 * Math.max(1 - quality, 0.25F))));
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, (int) (6000 * Math.max(1 - quality, 0.25F))));
		}
	});

	public static final FluidEntry IRON_WINE = booze("iron_wine", 0.5F, props(1034, 1400), (level, player, quality) -> {
		if (quality >= 0.5F) {
			float absorption = 10F * Math.max((quality - 0.5F) * 2F, 0F);
			player.getFoodData().eat(1, 2F * quality);
			player.setAbsorptionAmount(Math.max(Math.min(player.getAbsorptionAmount() + absorption, 20F), player.getAbsorptionAmount()));
		} else {
			int duration = (int) (6000 * Math.max(1 - quality, 0.25));
			float damage = 10F * Math.max(Math.abs(quality - 0.5F) + 0.1F, 0.25F);
			player.hurt(level.damageSources().magic(), damage);
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, duration));
		}
	});

	public static final FluidEntry MEAD = booze("mead", 0.5F, props(1034, 1500), (level, player, quality) -> {
		if (quality >= 0.5F) {
			player.getFoodData().eat(1, 2F * quality);
			int duration = 1200 + (int) (6000 * Math.max(Math.abs((quality - 0.5F) * 2F), 0F));
			player.addEffect(new MobEffectInstance(ModEffects.WITHER_WARD, duration));
		} else {
			player.addEffect(new MobEffectInstance(MobEffects.WITHER, (int) (800 * Math.max(1 - quality, 0.25))));
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, (int) (6000 * Math.max(1 - quality, 0.25))));
		}
	});

	public static final FluidEntry WILDBERRY_WINE = booze("wildberry_wine", 0.85F, props(1034, 1500), (level, player, quality) -> {
		if (quality >= 0.5F) {
			player.getFoodData().eat(1, 2F * quality);
			for (MobEffectInstance effect : List.copyOf(player.getActiveEffects())) {
				int maxAmplifier = nadiendev.rusticrevived.config.RusticConfig.COMMON.getWildberryMaxAmplifier(
						effect.getEffect().unwrapKey().map(k -> k.identifier()).orElse(null));
				if (effect.getEffect().value().isBeneficial() && effect.getAmplifier() < maxAmplifier) {
					player.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier() + 1, effect.isAmbient(), effect.isVisible()));
				}
			}
		} else {
			for (MobEffectInstance effect : List.copyOf(player.getActiveEffects())) {
				if (effect.getEffect().value().isBeneficial()) {
					player.removeEffect(effect.getEffect());
					if (effect.getAmplifier() > 0) {
						player.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier() - 1, effect.isAmbient(), effect.isVisible()));
					}
				}
			}
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, (int) (6000 * Math.max(1 - quality, 0.25F))));
		}
	});

	public static final FluidEntry WINE = booze("wine", 0.5F, props(1034, 1500), (level, player, quality) -> {
		if (quality >= 0.5F) {
			player.getFoodData().eat(1, 2F * quality);
			int durationIncrease = 600 + (int) (2400 * ((quality - 0.5F) * 2F));
			for (MobEffectInstance effect : List.copyOf(player.getActiveEffects())) {
				if (effect.getEffect().value().isBeneficial() && effect.getDuration() < 12000 && !effect.isInfiniteDuration()) {
					int duration = Math.max(Math.min(effect.getDuration() + durationIncrease, 12000), effect.getDuration());
					player.addEffect(new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
				}
			}
		} else {
			int durationDecrease = (int) (2400 * Math.abs(quality - 0.6));
			for (MobEffectInstance effect : List.copyOf(player.getActiveEffects())) {
				if (effect.getEffect().value().isBeneficial() && !effect.isInfiniteDuration()) {
					int duration = effect.getDuration() - durationDecrease;
					player.removeEffect(effect.getEffect());
					if (duration > 0) {
						player.addEffect(new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
					}
				}
			}
			player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, (int) (6000 * Math.max(1 - quality, 0))));
		}
	});

	public static final FluidEntry AMBROSIA = register("ambrosia", true, false, 1, () -> new BoozeFluidType(props(1004, 1400).rarity(Rarity.EPIC), 1.0F,
			(level, player, quality) -> {
				if (quality >= 0.5F) {
					player.getFoodData().eat(2, 2F * quality);
					int duration;
					if (quality > 0.99F) {
						duration = MobEffectInstance.INFINITE_DURATION;
						player.removeEffect(ModEffects.UNDYING);
					} else {
						duration = 20 * 300 + (int) (18000 * Math.max(Math.abs((quality - 0.5F) * 2F), 0F));
					}
					player.addEffect(new MobEffectInstance(ModEffects.UNDYING, duration, 0, false, false));
				} else {
					player.hurt(RusticDamage.badAmbrosia(level), Float.MAX_VALUE);
				}
			}) {
		@Override
		protected void inebriate(Level level, Player player, float quality) {
			int duration = quality >= 0.5F
					? (int) (12000 * Math.max(1 - Math.abs(quality - 0.75F), 0.5F))
					: (int) (12000 * Math.max(1 - quality * 0.5F, 0.5F));
			MobEffectInstance tipsy = player.getEffect(ModEffects.TIPSY);
			if (level.getRandom().nextFloat() < inebriationChance) {
				if (tipsy == null) {
					player.addEffect(new MobEffectInstance(ModEffects.TIPSY, duration, quality > 0.99F ? 1 : 0, false, false));
				} else if (tipsy.getAmplifier() < 3) {
					int newAmp = Math.min(tipsy.getAmplifier() + (quality > 0.99F ? 2 : 1), 3);
					player.addEffect(new MobEffectInstance(ModEffects.TIPSY, Math.max(duration, tipsy.getDuration()), newAmp, false, false));
				}
			}
		}
	}, null);

	private ModFluids() {
	}

	private static void addFullmetal(Player player, int duration) {
		MobEffectInstance effect = player.getEffect(ModEffects.FULLMETAL);
		player.addEffect(new MobEffectInstance(ModEffects.FULLMETAL, effect == null ? duration : effect.getDuration() + duration, 0, false, true));
	}

	private static FluidType.Properties props(int density, int viscosity) {
		return FluidType.Properties.create()
				.density(density)
				.viscosity(viscosity)
				.canExtinguish(true)
				.supportsBoating(true)
				.canHydrate(false)
				.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
				.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
	}

	private static FluidEntry juice(String name, FluidType.Properties props, int levelDecrease, DrinkableFluidType.DrinkEffect effect,
			@Nullable RusticLiquidBlock.EntityInside inside) {
		return register(name, false, true, levelDecrease, () -> new DrinkableFluidType(props, effect), inside);
	}

	private static FluidEntry booze(String name, float inebriation, FluidType.Properties props, BoozeFluidType.QualityEffect effect) {
		return register(name, true, false, 1, () -> new BoozeFluidType(props, inebriation, effect), null);
	}

	private static FluidEntry register(String name, boolean booze, boolean placeable, int levelDecrease, Supplier<? extends FluidType> type,
			@Nullable RusticLiquidBlock.EntityInside inside) {
		FluidEntry entry = new FluidEntry(name, booze, placeable, levelDecrease, type, inside);
		ALL.add(entry);
		(booze ? BOOZE : JUICES).add(entry);
		return entry;
	}

	/**
	 * Everything registered for one Rustic fluid.
	 */
	public static final class FluidEntry {
		public final String name;
		public final boolean booze;
		public final DeferredHolder<FluidType, FluidType> type;
		public final DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
		public final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
		/** null for booze */
		@Nullable
		public final DeferredBlock<RusticLiquidBlock> block;
		/** null for booze */
		@Nullable
		public final DeferredItem<BucketItem> bucket;
		private final BaseFlowingFluid.Properties properties;

		FluidEntry(String name, boolean booze, boolean placeable, int levelDecrease, Supplier<? extends FluidType> typeFactory,
				@Nullable RusticLiquidBlock.EntityInside inside) {
			this.name = name;
			this.booze = booze;
			this.type = FLUID_TYPES.register(name, typeFactory);
			this.source = FLUIDS.register(name, () -> new BaseFlowingFluid.Source(properties()));
			this.flowing = FLUIDS.register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(properties()));
			BaseFlowingFluid.Properties p = new BaseFlowingFluid.Properties(type, source, flowing)
					.levelDecreasePerBlock(levelDecrease)
					.slopeFindDistance(levelDecrease > 1 ? 2 : 4);
			if (placeable) {
				this.block = ModBlocks.BLOCKS.registerBlock(name, props -> new RusticLiquidBlock(source, props, inside),
						() -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_YELLOW));
				this.bucket = ModItems.ITEMS.registerItem(name + "_bucket", props -> new BucketItem(source.get(), props),
						() -> new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));
				p.block(block).bucket(bucket);
			} else {
				this.block = null;
				this.bucket = null;
			}
			this.properties = p;
		}

		private BaseFlowingFluid.Properties properties() {
			return properties;
		}

		public Fluid get() {
			return source.get();
		}

		public FluidType getType() {
			return type.get();
		}

		public Identifier stillTexture() {
			return RusticRevived.id(booze ? "block/fluids/booze/" + name + "_still" : "block/fluids/" + name + "_still");
		}

		public Identifier flowingTexture() {
			return RusticRevived.id(booze ? "block/fluids/booze/" + name + "_flow" : "block/fluids/" + name + "_flow");
		}
	}
}
