package nadiendev.rusticrevived.registry;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.blockentity.alchemy.AdvancedCondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.BrewingBarrelBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.CondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.CrushingTubBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.EvaporatingBasinBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.LiquidBarrelBlockEntity;
import nadiendev.rusticrevived.blockentity.storage.ApiaryBlockEntity;
import nadiendev.rusticrevived.blockentity.storage.CabinetBlockEntity;
import nadiendev.rusticrevived.blockentity.storage.RusticBarrelBlockEntity;
import nadiendev.rusticrevived.blockentity.storage.VaseBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RusticRevived.NAMESPACE);

	// storage
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VaseBlockEntity>> VASE = BLOCK_ENTITIES.register("vase",
			() -> BlockEntityType.Builder.of(VaseBlockEntity::new, ModBlocks.VASE.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RusticBarrelBlockEntity>> BARREL = BLOCK_ENTITIES.register("barrel",
			() -> BlockEntityType.Builder.of(RusticBarrelBlockEntity::new, ModBlocks.BARREL.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CabinetBlockEntity>> CABINET = BLOCK_ENTITIES.register("cabinet",
			() -> BlockEntityType.Builder.of(CabinetBlockEntity::new, ModBlocks.CABINET.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ApiaryBlockEntity>> APIARY = BLOCK_ENTITIES.register("apiary",
			() -> BlockEntityType.Builder.of(ApiaryBlockEntity::new, ModBlocks.APIARY.get()).build(null));

	// alchemy & brewing
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CondenserBlockEntity>> CONDENSER = BLOCK_ENTITIES.register("condenser",
			() -> BlockEntityType.Builder.of(CondenserBlockEntity::new, ModBlocks.CONDENSER.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedCondenserBlockEntity>> CONDENSER_ADVANCED = BLOCK_ENTITIES.register("condenser_advanced",
			() -> BlockEntityType.Builder.of(AdvancedCondenserBlockEntity::new, ModBlocks.CONDENSER_ADVANCED.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrewingBarrelBlockEntity>> BREWING_BARREL = BLOCK_ENTITIES.register("brewing_barrel",
			() -> BlockEntityType.Builder.of(BrewingBarrelBlockEntity::new, ModBlocks.BREWING_BARREL.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrushingTubBlockEntity>> CRUSHING_TUB = BLOCK_ENTITIES.register("crushing_tub",
			() -> BlockEntityType.Builder.of(CrushingTubBlockEntity::new, ModBlocks.CRUSHING_TUB.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EvaporatingBasinBlockEntity>> EVAPORATING_BASIN = BLOCK_ENTITIES.register("evaporating_basin",
			() -> BlockEntityType.Builder.of(EvaporatingBasinBlockEntity::new, ModBlocks.EVAPORATING_BASIN.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LiquidBarrelBlockEntity>> LIQUID_BARREL = BLOCK_ENTITIES.register("liquid_barrel",
			() -> BlockEntityType.Builder.of(LiquidBarrelBlockEntity::new, ModBlocks.LIQUID_BARREL.get()).build(null));

	private ModBlockEntities() {
	}
}
