package nadiendev.rusticrevived.setup;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.block.alchemy.AbstractCondenserBlock;
import nadiendev.rusticrevived.blockentity.alchemy.AbstractCondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.LiquidBarrelBlockEntity;
import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.item.FluidBottleItem;
import nadiendev.rusticrevived.item.LiquidBarrelItem;
import nadiendev.rusticrevived.recipe.VantaOilRecipe;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.RusticCreativeContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * Common (both sides) setup of the alchemy subsystem: capabilities, creative stacks and the
 * legacy EventHandlerCommon features (glass bottle filling, olive oiled food, vanta oiled
 * weapons). Called from {@link CommonSetup}.
 */
public final class AlchemySetup {
	/** Quality of the booze shown in the creative tab (legacy getSubItems). */
	private static final float CREATIVE_BOOZE_QUALITY = 0.75F;
	private static final int VANTA_PARTICLES = 20;

	private AlchemySetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} listeners here. */
	public static void init(IEventBus modBus) {
		NeoForge.EVENT_BUS.addListener(AlchemySetup::onUseGlassBottleOnBlock);
		NeoForge.EVENT_BUS.addListener(AlchemySetup::onUseGlassBottle);
		NeoForge.EVENT_BUS.addListener(AlchemySetup::onFinishUsingItem);
		NeoForge.EVENT_BUS.addListener(AlchemySetup::onAttackEntity);
		RusticCreativeContents.ALCHEMY_EXTRAS.add(AlchemySetup::addCreativeStacks);
	}

	/** Called from {@link RegisterCapabilitiesEvent}. */
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		Block[] condensers = { ModBlocks.CONDENSER.get(), ModBlocks.CONDENSER_ADVANCED.get() };
		event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
			AbstractCondenserBlockEntity condenser = automatedCondenser(level, pos, state);
			return condenser == null ? null : condenser.getItemHandler(side);
		}, condensers);
		event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
			AbstractCondenserBlockEntity condenser = automatedCondenser(level, pos, state);
			return condenser == null ? null : condenser.getTank();
		}, condensers);

		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.BREWING_BARREL.get(),
				(barrel, side) -> barrel.getAutomationHandler());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CRUSHING_TUB.get(), (tub, side) -> tub.getItems());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.CRUSHING_TUB.get(), (tub, side) -> tub.getFluidHandler());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.EVAPORATING_BASIN.get(),
				(basin, side) -> basin.getAutomationHandler());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.EVAPORATING_BASIN.get(), (basin, side) -> basin.getTank());
		event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.LIQUID_BARREL.get(), (barrel, side) -> barrel.getTank());

		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new FluidBottleItem.FluidHandler(stack), ModItems.FLUID_BOTTLE.get());
		event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new LiquidBarrelItem.FluidHandler(stack),
				ModBlocks.LIQUID_BARREL.get().asItem());
	}

	/** Called during FMLCommonSetupEvent (already inside enqueueWork: not thread safe work is fine). */
	public static void commonSetup() {
	}

	/**
	 * The condenser automation reaches from this block: the bottom half of a condenser, or the
	 * top half of an advanced one; only while the retorts are attached.
	 */
	@Nullable
	private static AbstractCondenserBlockEntity automatedCondenser(Level level, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof AbstractCondenserBlock block && (state.getValue(AbstractCondenserBlock.BOTTOM) || block.isAdvanced())) {
			return block.getCondenser(level, pos, state);
		}
		return null;
	}

	/** Filled fluid bottles and liquid barrels of every Rustic fluid. */
	private static void addCreativeStacks(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
		ItemStack barrel = new ItemStack(ModBlocks.LIQUID_BARREL.get());
		output.accept(LiquidBarrelItem.filled(barrel, new FluidStack(Fluids.WATER, LiquidBarrelBlockEntity.CAPACITY)));
		for (ModFluids.FluidEntry entry : ModFluids.ALL) {
			FluidStack fluid = new FluidStack(entry.get(), LiquidBarrelBlockEntity.CAPACITY);
			if (entry.booze) {
				fluid = BoozeFluidType.withQuality(fluid, CREATIVE_BOOZE_QUALITY);
			}
			if (FluidBottleItem.canHold(fluid)) {
				output.accept(FluidBottleItem.filled(fluid));
			}
			output.accept(LiquidBarrelItem.filled(barrel, fluid));
		}
	}

	// ---------------------------------------------------------------- glass bottles

	/** Glass bottle used on a block with a tank: bottles drinkable fluids (water gives a water bottle). */
	private static void onUseGlassBottleOnBlock(PlayerInteractEvent.RightClickBlock event) {
		ItemStack stack = event.getItemStack();
		if (!stack.is(Items.GLASS_BOTTLE)) return;
		Level level = event.getLevel();
		IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, event.getPos(), event.getFace());
		if (tank == null) return;
		FluidStack available = tank.drain(FluidBottleItem.CAPACITY, IFluidHandler.FluidAction.SIMULATE);
		if (available.getAmount() < FluidBottleItem.CAPACITY) return;
		ItemStack filled;
		if (FluidBottleItem.canHold(available)) {
			filled = FluidBottleItem.filled(available);
		} else if (available.getFluid() == Fluids.WATER) {
			filled = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		} else {
			return;
		}
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
		if (!level.isClientSide) {
			tank.drain(FluidBottleItem.CAPACITY, IFluidHandler.FluidAction.EXECUTE);
			fillBottle(event.getEntity(), event.getHand(), stack, filled);
		}
	}

	/** Glass bottle used on a source block of a drinkable Rustic fluid: bottles it, removing the source. */
	private static void onUseGlassBottle(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		if (!stack.is(Items.GLASS_BOTTLE)) return;
		Level level = event.getLevel();
		Player player = event.getEntity();
		Vec3 eye = player.getEyePosition();
		Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
		if (hit.getType() != HitResult.Type.BLOCK) return;
		BlockPos pos = hit.getBlockPos();
		FluidState fluidState = level.getFluidState(pos);
		FluidStack fluid = new FluidStack(fluidState.getType(), FluidBottleItem.CAPACITY);
		if (!fluidState.isSource() || !FluidBottleItem.canHold(fluid) || !level.mayInteract(player, pos)
				|| !player.mayUseItemAt(pos, hit.getDirection(), stack)) {
			return;
		}
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
		if (!level.isClientSide) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
			level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
			fillBottle(player, event.getHand(), stack, FluidBottleItem.filled(fluid));
		}
	}

	private static void fillBottle(Player player, InteractionHand hand, ItemStack bottle, ItemStack filled) {
		player.awardStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
		player.setItemInHand(hand, ItemUtils.createFilledResult(bottle, player, filled));
	}

	// ---------------------------------------------------------------- olive oil

	/** Olive oiled food fills 2 more hunger points (legacy onItemUseFinish / onItemUseTick). */
	private static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
		ItemStack stack = event.getItem();
		if (event.getEntity() instanceof Player player && !player.level().isClientSide && stack.has(ModDataComponents.OLIVE_OILED)
				&& stack.getFoodProperties(player) != null) {
			player.getFoodData().eat(2, 0.3F);
		}
	}

	// ---------------------------------------------------------------- vanta oil

	/** A vanta oiled weapon applies one hit worth of its effect to the struck entity. */
	private static void onAttackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		ItemStack stack = player.getMainHandItem();
		MobEffectInstance total = VantaOilRecipe.getOil(stack);
		if (total == null || !(event.getTarget() instanceof LivingEntity target) || !target.isAttackable() || !target.isAffectedByPotions()
				|| target.skipAttackInteraction(player)) {
			return;
		}
		Holder<MobEffect> effect = total.getEffect();
		boolean instant = effect.value().isInstantenous();
		int amplifier = total.getAmplifier();
		int hitDuration = instant ? 1 : VantaOilRecipe.nextHitDuration(total.getDuration());
		int appliedDuration = hitDuration;
		MobEffectInstance active = target.getEffect(effect);
		if (active != null) {
			if (active.getAmplifier() == amplifier) {
				appliedDuration += active.getDuration();
			} else if (active.getAmplifier() > amplifier) {
				appliedDuration = 0;
			}
		}
		Level level = player.level();
		if (level.isClientSide) {
			if (appliedDuration > 0) {
				spawnVantaParticles(level, target, effect.value().getColor());
			}
			return;
		}
		if (appliedDuration > 0) {
			if (instant) {
				effect.value().applyInstantenousEffect(player, player, target, amplifier, 1.0D);
			} else {
				target.addEffect(new MobEffectInstance(effect, appliedDuration, amplifier), player);
			}
		}
		if (stack.getCount() > 1) {
			// only one weapon of the stack loses its oil
			ItemStack rest = stack.split(stack.getCount() - 1);
			if (!player.getInventory().add(rest)) {
				player.drop(rest, false);
			}
		}
		int left = total.getDuration() - hitDuration;
		if (left <= 0) {
			stack.remove(ModDataComponents.VANTA_OIL);
		} else {
			stack.set(ModDataComponents.VANTA_OIL, VantaOilRecipe.createOil(new MobEffectInstance(effect, left, amplifier)));
		}
	}

	private static void spawnVantaParticles(Level level, LivingEntity target, int color) {
		ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, (color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F,
				(color & 255) / 255F);
		for (int i = 0; i < VANTA_PARTICLES; i++) {
			level.addParticle(particle, target.getRandomX(0.5D), target.getRandomY(), target.getRandomZ(0.5D), 0, 0, 0);
		}
	}
}
