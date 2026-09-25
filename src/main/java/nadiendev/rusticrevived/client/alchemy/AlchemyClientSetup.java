package nadiendev.rusticrevived.client.alchemy;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.VantaOilRecipe;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModMenus;
import nadiendev.rusticrevived.registry.ModRecipes;
import nadiendev.rusticrevived.registry.RusticCreativeContents;
import net.minecraft.ChatFormatting;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

/**
 * Client setup of the alchemy subsystem: screens, renderers, item model types (bottle fluid
 * select property, booze label tint, liquid barrel special renderer), tooltips and the elixir
 * creative entries. Called from {@link nadiendev.rusticrevived.client.ClientSetup} on the
 * physical client only.
 */
public final class AlchemyClientSetup {
	/** Condenser recipes synced by the server (the client has no recipe manager). */
	private static List<RecipeHolder<CondenserRecipe>> condenserRecipes = List.of();

	private AlchemyClientSetup() {
	}

	/** Register mod bus and {@code NeoForge.EVENT_BUS} client listeners here. */
	public static void init(IEventBus modBus) {
		modBus.addListener(AlchemyClientSetup::registerScreens);
		modBus.addListener(AlchemyClientSetup::registerRenderers);
		modBus.addListener(AlchemyClientSetup::registerItemTintSources);
		modBus.addListener(AlchemyClientSetup::registerSelectProperties);
		modBus.addListener(AlchemyClientSetup::registerSpecialRenderers);
		NeoForge.EVENT_BUS.addListener(AlchemyClientSetup::onTooltip);
		NeoForge.EVENT_BUS.addListener(AlchemyClientSetup::onRecipesReceived);
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

	private static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
		event.register(BoozeLabelTint.ID, BoozeLabelTint.MAP_CODEC);
	}

	private static void registerSelectProperties(RegisterSelectItemModelPropertyEvent event) {
		event.register(BottleFluidProperty.ID, BottleFluidProperty.TYPE);
	}

	private static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
		event.register(LiquidBarrelItemRenderer.ID, LiquidBarrelItemRenderer.Unbaked.MAP_CODEC);
	}

	private static void onRecipesReceived(RecipesReceivedEvent event) {
		condenserRecipes = event.getRecipeMap().byType(ModRecipes.CONDENSER.get()).stream()
				.sorted(Comparator.comparing(holder -> holder.id().identifier().toString())).toList();
	}

	/** Every elixir brewed by a condenser recipe (legacy ItemElixir.getSubItems); needs the synced recipes. */
	private static void addElixirs(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
		Set<ItemStack> added = ItemStackLinkedSet.createTypeAndComponentsSet();
		for (RecipeHolder<CondenserRecipe> recipe : condenserRecipes) {
			ItemStack result = recipe.value().getResultStack();
			if (!result.isEmpty() && added.add(result)) {
				output.accept(result.copy());
			}
		}
	}

	// ---------------------------------------------------------------- tooltips (legacy EventHandlerClient)

	private static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		List<Component> tooltip = event.getToolTip();
		if (stack.has(ModDataComponents.OLIVE_OILED.get())) {
			tooltip.add(Component.translatable("tooltip.rusticrevived.olive_oil").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
		}
		MobEffectInstance vantaOil = VantaOilRecipe.getOil(stack);
		if (vantaOil != null) {
			addVantaOilTooltip(stack, vantaOil, tooltip, event.getContext().tickRate());
		}
		FluidStack fluid = FluidUtil.getFirstStackContained(stack);
		if (fluid.getFluidType() instanceof BoozeFluidType && BoozeFluidType.hasQuality(fluid)) {
			tooltip.add(BoozeQuality.tooltip(BoozeFluidType.getQuality(fluid)));
		}
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
		ModDataComponents.VantaOil oil = stack.get(ModDataComponents.VANTA_OIL.get());
		int uses = oil == null ? 0 : oil.uses();

		tooltip.add(Component.empty());
		tooltip.add(Component.translatable("tooltip.rusticrevived.vanta_oil").withStyle(ChatFormatting.DARK_PURPLE));
		tooltip.add(Component.literal(" ").append(name));
		tooltip.add(Component.literal(" ").append(Component.translatable(uses == 1 ? "tooltip.rusticrevived.vanta_oil_use" : "tooltip.rusticrevived.vanta_oil_uses",
				uses)).withStyle(ChatFormatting.GRAY));
	}
}
