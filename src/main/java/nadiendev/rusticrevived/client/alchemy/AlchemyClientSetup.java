package nadiendev.rusticrevived.client.alchemy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.item.ElixirItem;
import nadiendev.rusticrevived.item.FluidBottleItem;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.VantaOilRecipe;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModMenus;
import nadiendev.rusticrevived.registry.ModRecipes;
import nadiendev.rusticrevived.registry.RusticCreativeContents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * Client setup of the alchemy subsystem: screens, renderers, colors, models, client events.
 * Called from {@link nadiendev.rusticrevived.client.ClientSetup} on the physical client only.
 */
public final class AlchemyClientSetup {
	/** Label colour of booze bottles without a quality. */
	private static final float DEFAULT_LABEL_QUALITY = 0.5F;
	/** Elixir colour without effects (legacy ElixirUtils.getColor). */
	private static final int NO_EFFECT_COLOR = 0xFFF800F8;

	private AlchemyClientSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} client listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(AlchemyClientSetup::registerScreens);
		modBus.addListener(AlchemyClientSetup::registerRenderers);
		modBus.addListener(AlchemyClientSetup::registerItemColors);
		modBus.addListener(AlchemyClientSetup::registerClientExtensions);
		modBus.addListener(AlchemyClientSetup::clientSetup);
		NeoForge.EVENT_BUS.addListener(AlchemyClientSetup::onTooltip);
		RusticCreativeContents.ALCHEMY_EXTRAS.add(AlchemyClientSetup::addElixirs);
	}

	private static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModMenus.CONDENSER.get(), CondenserScreen::new);
		event.register(ModMenus.CONDENSER_ADVANCED.get(), AdvancedCondenserScreen::new);
		event.register(ModMenus.BREWING_BARREL.get(), BrewingBarrelScreen::new);
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlockEntities.CRUSHING_TUB.get(), CrushingTubRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.EVAPORATING_BASIN.get(), EvaporatingBasinRenderer::new);
		event.registerBlockEntityRenderer(ModBlockEntities.LIQUID_BARREL.get(), LiquidBarrelRenderer::new);
	}

	private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		// fluid bottles: layer 1 is the fluid, layer 2 the booze label
		event.register((stack, tintIndex) -> {
			FluidStack fluid = FluidBottleItem.getFluid(stack);
			if (tintIndex == 1 && !fluid.isEmpty()) {
				return FluidRendering.tint(fluid);
			}
			if (tintIndex == 2) {
				return BoozeQuality.labelColor(BoozeFluidType.hasQuality(fluid) ? BoozeFluidType.getQuality(fluid) : DEFAULT_LABEL_QUALITY);
			}
			return 0xFFFFFFFF;
		}, ModItems.FLUID_BOTTLE.get());
		event.register((stack, tintIndex) -> {
			if (tintIndex != 0) return 0xFFFFFFFF;
			return ElixirItem.getContents(stack).hasEffects() ? 0xFF000000 | ElixirItem.getContents(stack).getColor() : NO_EFFECT_COLOR;
		}, ModItems.ELIXIR.get());
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(new IClientItemExtensions() {
			private BlockEntityWithoutLevelRenderer renderer;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (renderer == null) {
					renderer = new LiquidBarrelItemRenderer(Minecraft.getInstance());
				}
				return renderer;
			}
		}, ModBlocks.LIQUID_BARREL.get().asItem());
	}

	private static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> ItemProperties.register(ModItems.FLUID_BOTTLE.get(), FluidBottleItem.BOOZE_MODEL_PROPERTY,
				(stack, level, entity, seed) -> FluidBottleItem.boozeModel(stack)));
	}

	/** Every elixir brewed by a condenser recipe (legacy ItemElixir.getSubItems); needs the client recipes. */
	private static void addElixirs(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;
		List<RecipeHolder<CondenserRecipe>> recipes = minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipes.CONDENSER.get()).stream()
				.sorted(Comparator.comparing(holder -> holder.id().toString())).toList();
		Set<ItemStack> added = ItemStackLinkedSet.createTypeAndComponentsSet();
		for (RecipeHolder<CondenserRecipe> recipe : recipes) {
			ItemStack result = recipe.value().getResultItem(parameters.holders());
			if (!result.isEmpty() && added.add(result)) {
				output.accept(result.copy());
			}
		}
	}

	// ---------------------------------------------------------------- tooltips (legacy EventHandlerClient)

	private static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		List<Component> tooltip = event.getToolTip();
		if (stack.has(ModDataComponents.OLIVE_OILED)) {
			tooltip.add(Component.translatable("tooltip.rusticrevived.olive_oil").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
		}
		MobEffectInstance vantaOil = VantaOilRecipe.getOil(stack);
		if (vantaOil != null) {
			addVantaOilTooltip(stack, vantaOil, tooltip, event.getContext().tickRate());
		}
		FluidUtil.getFluidContained(stack)
				.filter(fluid -> fluid.getFluidType() instanceof BoozeFluidType && BoozeFluidType.hasQuality(fluid))
				.ifPresent(fluid -> tooltip.add(BoozeQuality.tooltip(BoozeFluidType.getQuality(fluid))));
	}

	private static void addVantaOilTooltip(ItemStack stack, MobEffectInstance total, List<Component> tooltip, float tickRate) {
		Holder<MobEffect> effect = total.getEffect();
		MutableComponent name = Component.translatable(effect.value().getDescriptionId());
		if (total.getAmplifier() > 0) {
			name.append(" ").append(Component.translatable("potion.potency." + total.getAmplifier()));
		}
		if (!effect.value().isInstantenous()) {
			MobEffectInstance hit = new MobEffectInstance(effect, VantaOilRecipe.nextHitDuration(total.getDuration()));
			name.append(" (").append(MobEffectUtil.formatDuration(hit, 1.0F, tickRate)).append(")");
		}
		name.withStyle(effect.value().getCategory() == MobEffectCategory.HARMFUL ? ChatFormatting.RED : ChatFormatting.BLUE);
		ModDataComponents.VantaOil oil = stack.get(ModDataComponents.VANTA_OIL);
		int uses = oil == null ? 0 : oil.uses();

		tooltip.add(Component.empty());
		tooltip.add(Component.translatable("tooltip.rusticrevived.vanta_oil").withStyle(ChatFormatting.DARK_PURPLE));
		tooltip.add(Component.literal(" ").append(name));
		tooltip.add(Component.literal(" ").append(Component.translatable(uses == 1 ? "tooltip.rusticrevived.vanta_oil_use" : "tooltip.rusticrevived.vanta_oil_uses",
				uses)).withStyle(ChatFormatting.GRAY));
	}
}
