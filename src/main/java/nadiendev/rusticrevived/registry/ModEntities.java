package nadiendev.rusticrevived.registry;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.entity.ChairSeatEntity;
import nadiendev.rusticrevived.entity.TomatoEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, RusticRevived.NAMESPACE);

	public static final DeferredHolder<EntityType<?>, EntityType<TomatoEntity>> TOMATO = ENTITIES.register("tomato",
			key -> EntityType.Builder.<TomatoEntity>of(TomatoEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
					.clientTrackingRange(4).updateInterval(10).build(ResourceKey.create(Registries.ENTITY_TYPE, key)));

	public static final DeferredHolder<EntityType<?>, EntityType<ChairSeatEntity>> CHAIR = ENTITIES.register("chair",
			key -> EntityType.Builder.<ChairSeatEntity>of(ChairSeatEntity::new, MobCategory.MISC).sized(0.0001F, 0.0001F)
					.noSummon().fireImmune().clientTrackingRange(8).updateInterval(Integer.MAX_VALUE).build(ResourceKey.create(Registries.ENTITY_TYPE, key)));

	private ModEntities() {
	}
}
