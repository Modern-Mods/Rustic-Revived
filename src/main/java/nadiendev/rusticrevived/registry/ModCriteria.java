package nadiendev.rusticrevived.registry;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.advancement.BoozeItemPredicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Advancement criteria triggers and item (data component) predicates (legacy AlcoholItemPredicate).
 */
@EventBusSubscriber(modid = RusticRevived.MODID)
public final class ModCriteria {
	public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, RusticRevived.NAMESPACE);

	/** {@code rusticrevived:booze}: booze containers by fluid and quality (used with inventory_changed). */
	public static final DataComponentPredicate.Type<BoozeItemPredicate> BOOZE = new DataComponentPredicate.ConcreteType<>(BoozeItemPredicate.CODEC);

	private ModCriteria() {
	}

	@SubscribeEvent
	public static void registerItemPredicates(RegisterEvent event) {
		event.register(Registries.DATA_COMPONENT_PREDICATE_TYPE, helper -> helper.register(RusticRevived.id("booze"), BOOZE));
	}
}
