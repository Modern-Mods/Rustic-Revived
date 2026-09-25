package nadiendev.rusticrevived.registry;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.advancement.BoozeItemPredicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Advancement criteria triggers and item sub predicates (legacy AlcoholItemPredicate).
 */
@EventBusSubscriber(modid = RusticRevived.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCriteria {
	public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, RusticRevived.NAMESPACE);

	/** {@code rusticrevived:booze}: booze containers by fluid and quality (used with inventory_changed). */
	public static final ItemSubPredicate.Type<BoozeItemPredicate> BOOZE = new ItemSubPredicate.Type<>(BoozeItemPredicate.CODEC);

	private ModCriteria() {
	}

	@SubscribeEvent
	public static void registerItemSubPredicates(RegisterEvent event) {
		event.register(Registries.ITEM_SUB_PREDICATE_TYPE, helper -> helper.register(RusticRevived.id("booze"), BOOZE));
	}
}
