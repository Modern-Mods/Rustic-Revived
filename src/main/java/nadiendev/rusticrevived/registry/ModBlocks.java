package nadiendev.rusticrevived.registry;

import java.util.EnumMap;
import java.util.Map;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.WoodVariant;
import nadiendev.rusticrevived.block.alchemy.AdvancedCondenserBlock;
import nadiendev.rusticrevived.block.alchemy.BrewingBarrelBlock;
import nadiendev.rusticrevived.block.alchemy.CondenserBlock;
import nadiendev.rusticrevived.block.alchemy.CrushingTubBlock;
import nadiendev.rusticrevived.block.alchemy.EvaporatingBasinBlock;
import nadiendev.rusticrevived.block.alchemy.LiquidBarrelBlock;
import nadiendev.rusticrevived.block.alchemy.RetortBlock;
import nadiendev.rusticrevived.block.decor.CandleLeverBlock;
import nadiendev.rusticrevived.block.decor.ChairBlock;
import nadiendev.rusticrevived.block.decor.ChandelierBlock;
import nadiendev.rusticrevived.block.decor.ClayWallDiagBlock;
import nadiendev.rusticrevived.block.decor.DoubleCandleBlock;
import nadiendev.rusticrevived.block.decor.GargoyleBlock;
import nadiendev.rusticrevived.block.decor.IronTorchBlock;
import nadiendev.rusticrevived.block.decor.LatticeBlock;
import nadiendev.rusticrevived.block.decor.RopeBlock;
import nadiendev.rusticrevived.block.decor.RusticCandleBlock;
import nadiendev.rusticrevived.block.decor.RusticChainBlock;
import nadiendev.rusticrevived.block.decor.RusticLanternBlock;
import nadiendev.rusticrevived.block.decor.TableBlock;
import nadiendev.rusticrevived.block.decor.WoodLanternBlock;
import nadiendev.rusticrevived.block.farm.AppleLeavesBlock;
import nadiendev.rusticrevived.block.farm.AppleSeedsBlock;
import nadiendev.rusticrevived.block.farm.BerryBushBlock;
import nadiendev.rusticrevived.block.farm.CropStakeBlock;
import nadiendev.rusticrevived.block.farm.FertileSoilBlock;
import nadiendev.rusticrevived.block.farm.GrapeLeavesBlock;
import nadiendev.rusticrevived.block.farm.GrapeStemBlock;
import nadiendev.rusticrevived.block.farm.HerbBlock;
import nadiendev.rusticrevived.block.farm.RusticLeavesBlock;
import nadiendev.rusticrevived.block.farm.RusticSaplingBlock;
import nadiendev.rusticrevived.block.farm.StakeCropBlock;
import nadiendev.rusticrevived.block.farm.StakeTiedBlock;
import nadiendev.rusticrevived.block.storage.ApiaryBlock;
import nadiendev.rusticrevived.block.storage.CabinetBlock;
import nadiendev.rusticrevived.block.storage.RusticBarrelBlock;
import nadiendev.rusticrevived.block.storage.RusticBeehiveBlock;
import nadiendev.rusticrevived.block.storage.VaseBlock;
import nadiendev.rusticrevived.world.ModTreeGrowers;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every Rustic block. Legacy metadata variants have been flattened into separate blocks
 * (planks/logs/leaves/saplings per wood, painted wood per color, chairs/tables per wood).
 * Block items are registered in {@link ModItems}.
 */
public final class ModBlocks {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RusticRevived.NAMESPACE);

	// ================================================================ decoration: stone

	public static final DeferredBlock<RotatedPillarBlock> STONE_PILLAR = pillar("stone_pillar", MapColor.STONE);
	public static final DeferredBlock<RotatedPillarBlock> ANDESITE_PILLAR = pillar("andesite_pillar", MapColor.STONE);
	public static final DeferredBlock<RotatedPillarBlock> DIORITE_PILLAR = pillar("diorite_pillar", MapColor.QUARTZ);
	public static final DeferredBlock<RotatedPillarBlock> GRANITE_PILLAR = pillar("granite_pillar", MapColor.DIRT);
	public static final DeferredBlock<RotatedPillarBlock> SLATE_PILLAR = pillar("slate_pillar", MapColor.COLOR_GRAY);
	/** PR #252 */
	public static final DeferredBlock<RotatedPillarBlock> BASALT_PILLAR = pillar("basalt_pillar", MapColor.COLOR_BLACK);
	/** PR #252, crafted from Quark limestone */
	public static final DeferredBlock<RotatedPillarBlock> LIMESTONE_PILLAR = pillar("limestone_pillar", MapColor.STONE);
	/** PR #252, crafted from marble (c:stones/marble) */
	public static final DeferredBlock<RotatedPillarBlock> MARBLE_PILLAR = pillar("marble_pillar", MapColor.QUARTZ);

	public static final DeferredBlock<Block> SLATE = stone("slate");
	public static final DeferredBlock<Block> SLATE_ROOF = stone("slate_roof");
	public static final DeferredBlock<Block> SLATE_TILE = stone("slate_tile");
	public static final DeferredBlock<Block> SLATE_BRICK = stone("slate_brick");
	public static final DeferredBlock<Block> SLATE_CHISELED = stone("slate_chiseled");
	/** PR #252 */
	public static final DeferredBlock<Block> SLATE_PAVEMENT = stone("slate_pavement");
	public static final DeferredBlock<StairBlock> SLATE_ROOF_STAIRS = BLOCKS.register("slate_roof_stairs",
			() -> new StairBlock(SLATE_ROOF.get().defaultBlockState(), stoneProps()));
	public static final DeferredBlock<SlabBlock> SLATE_ROOF_SLAB = BLOCKS.register("slate_roof_slab", () -> new SlabBlock(stoneProps()));
	public static final DeferredBlock<StairBlock> SLATE_BRICK_STAIRS = BLOCKS.register("slate_brick_stairs",
			() -> new StairBlock(SLATE_BRICK.get().defaultBlockState(), stoneProps()));
	public static final DeferredBlock<SlabBlock> SLATE_BRICK_SLAB = BLOCKS.register("slate_brick_slab", () -> new SlabBlock(stoneProps()));

	public static final DeferredBlock<Block> CLAY_WALL = BLOCKS.registerSimpleBlock("clay_wall", clayProps());
	public static final DeferredBlock<Block> CLAY_WALL_CROSS = BLOCKS.registerSimpleBlock("clay_wall_cross", clayProps());
	public static final DeferredBlock<ClayWallDiagBlock> CLAY_WALL_DIAG = BLOCKS.registerBlock("clay_wall_diag", ClayWallDiagBlock::new, clayProps());

	public static final DeferredBlock<GargoyleBlock> GARGOYLE = BLOCKS.registerBlock("gargoyle", GargoyleBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
					.strength(1.0F, 6.0F).noOcclusion());

	// ================================================================ decoration: painted wood

	public static final Map<DyeColor, DeferredBlock<Block>> PAINTED_WOOD = new EnumMap<>(DyeColor.class);

	static {
		for (DyeColor color : DyeColor.values()) {
			PAINTED_WOOD.put(color, BLOCKS.registerSimpleBlock("painted_wood_" + color.getSerializedName(),
					BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F)
							.sound(SoundType.WOOD).ignitedByLava()));
		}
	}

	// ================================================================ decoration: metal

	public static final DeferredBlock<RusticChainBlock> CHAIN = chain("chain");
	public static final DeferredBlock<RusticChainBlock> CHAIN_GOLD = chain("chain_gold");
	public static final DeferredBlock<RusticChainBlock> CHAIN_SILVER = chain("chain_silver");

	public static final DeferredBlock<RusticCandleBlock> CANDLE = BLOCKS.registerBlock("candle", RusticCandleBlock::new, candleProps());
	public static final DeferredBlock<RusticCandleBlock> CANDLE_GOLD = BLOCKS.registerBlock("candle_gold", RusticCandleBlock::new, candleProps());
	public static final DeferredBlock<RusticCandleBlock> CANDLE_SILVER = BLOCKS.registerBlock("candle_silver", RusticCandleBlock::new, candleProps());
	public static final DeferredBlock<DoubleCandleBlock> CANDLE_DOUBLE = BLOCKS.registerBlock("candle_double", DoubleCandleBlock::new, candleProps());
	public static final DeferredBlock<DoubleCandleBlock> CANDLE_DOUBLE_GOLD = BLOCKS.registerBlock("candle_double_gold", DoubleCandleBlock::new, candleProps());
	public static final DeferredBlock<DoubleCandleBlock> CANDLE_DOUBLE_SILVER = BLOCKS.registerBlock("candle_double_silver", DoubleCandleBlock::new, candleProps());
	public static final DeferredBlock<CandleLeverBlock> CANDLE_LEVER = BLOCKS.registerBlock("candle_lever", CandleLeverBlock::new, candleProps());
	public static final DeferredBlock<CandleLeverBlock> CANDLE_LEVER_GOLD = BLOCKS.registerBlock("candle_lever_gold", CandleLeverBlock::new, candleProps());
	public static final DeferredBlock<CandleLeverBlock> CANDLE_LEVER_SILVER = BLOCKS.registerBlock("candle_lever_silver", CandleLeverBlock::new, candleProps());

	public static final DeferredBlock<ChandelierBlock> CHANDELIER = chandelier("chandelier");
	public static final DeferredBlock<ChandelierBlock> CHANDELIER_GOLD = chandelier("chandelier_gold");
	public static final DeferredBlock<ChandelierBlock> CHANDELIER_SILVER = chandelier("chandelier_silver");

	public static final DeferredBlock<IronTorchBlock> IRON_TORCH = BLOCKS.registerBlock("iron_torch", IronTorchBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noCollission().instabreak().lightLevel(s -> 15).sound(SoundType.METAL)
					.pushReaction(PushReaction.DESTROY));

	public static final DeferredBlock<RusticLanternBlock> IRON_LANTERN = lantern("iron_lantern");
	public static final DeferredBlock<RusticLanternBlock> GOLDEN_LANTERN = lantern("golden_lantern");
	public static final DeferredBlock<RusticLanternBlock> SILVER_LANTERN = lantern("silver_lantern");
	public static final DeferredBlock<WoodLanternBlock> LANTERN_WOOD = BLOCKS.registerBlock("lantern_wood", WoodLanternBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).lightLevel(s -> 15).sound(SoundType.WOOD).noOcclusion()
					.pushReaction(PushReaction.DESTROY).ignitedByLava());

	public static final DeferredBlock<LatticeBlock> IRON_LATTICE = BLOCKS.registerBlock("iron_lattice", LatticeBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.0F, 6.0F).sound(SoundType.METAL)
					.noOcclusion());

	public static final DeferredBlock<RopeBlock> ROPE = BLOCKS.registerBlock("rope", RopeBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5F).sound(SoundType.WOOL).noOcclusion()
					.pushReaction(PushReaction.DESTROY).ignitedByLava());

	// ================================================================ decoration: furniture

	public static final Map<WoodVariant, DeferredBlock<ChairBlock>> CHAIRS = new EnumMap<>(WoodVariant.class);
	public static final Map<WoodVariant, DeferredBlock<TableBlock>> TABLES = new EnumMap<>(WoodVariant.class);

	static {
		for (WoodVariant wood : WoodVariant.values()) {
			CHAIRS.put(wood, BLOCKS.registerBlock("chair_" + wood.getSerializedName(), ChairBlock::new, furnitureProps(wood)));
			TABLES.put(wood, BLOCKS.registerBlock("table_" + wood.getSerializedName(), TableBlock::new, furnitureProps(wood)));
		}
	}

	// ================================================================ olive & ironwood

	public static final DeferredBlock<Block> OLIVE_PLANKS = BLOCKS.registerSimpleBlock("olive_planks", planksProps(MapColor.TERRACOTTA_WHITE));
	public static final DeferredBlock<Block> IRONWOOD_PLANKS = BLOCKS.registerSimpleBlock("ironwood_planks", planksProps(MapColor.COLOR_GRAY));
	public static final DeferredBlock<RotatedPillarBlock> OLIVE_LOG = BLOCKS.registerBlock("olive_log", RotatedPillarBlock::new, logProps(MapColor.TERRACOTTA_WHITE));
	public static final DeferredBlock<RotatedPillarBlock> IRONWOOD_LOG = BLOCKS.registerBlock("ironwood_log", RotatedPillarBlock::new, logProps(MapColor.COLOR_GRAY));
	public static final DeferredBlock<RusticLeavesBlock> OLIVE_LEAVES = BLOCKS.registerBlock("olive_leaves", RusticLeavesBlock::new, leavesProps());
	public static final DeferredBlock<RusticLeavesBlock> IRONWOOD_LEAVES = BLOCKS.registerBlock("ironwood_leaves", RusticLeavesBlock::new, leavesProps());
	public static final DeferredBlock<RusticSaplingBlock> OLIVE_SAPLING = BLOCKS.registerBlock("olive_sapling",
			p -> new RusticSaplingBlock(ModTreeGrowers.OLIVE, p), saplingProps());
	public static final DeferredBlock<RusticSaplingBlock> IRONWOOD_SAPLING = BLOCKS.registerBlock("ironwood_sapling",
			p -> new RusticSaplingBlock(ModTreeGrowers.IRONWOOD, p), saplingProps());
	public static final DeferredBlock<FenceBlock> OLIVE_FENCE = BLOCKS.registerBlock("olive_fence", FenceBlock::new, planksProps(MapColor.TERRACOTTA_WHITE));
	public static final DeferredBlock<FenceBlock> IRONWOOD_FENCE = BLOCKS.registerBlock("ironwood_fence", FenceBlock::new, planksProps(MapColor.COLOR_GRAY));
	public static final DeferredBlock<FenceGateBlock> OLIVE_FENCE_GATE = BLOCKS.registerBlock("olive_fence_gate",
			p -> new FenceGateBlock(WoodType.OAK, p), planksProps(MapColor.TERRACOTTA_WHITE));
	public static final DeferredBlock<FenceGateBlock> IRONWOOD_FENCE_GATE = BLOCKS.registerBlock("ironwood_fence_gate",
			p -> new FenceGateBlock(WoodType.OAK, p), planksProps(MapColor.COLOR_GRAY));
	public static final DeferredBlock<SlabBlock> OLIVE_SLAB = BLOCKS.registerBlock("olive_slab", SlabBlock::new, planksProps(MapColor.TERRACOTTA_WHITE));
	public static final DeferredBlock<SlabBlock> IRONWOOD_SLAB = BLOCKS.registerBlock("ironwood_slab", SlabBlock::new, planksProps(MapColor.COLOR_GRAY));
	public static final DeferredBlock<StairBlock> OLIVE_STAIRS = BLOCKS.register("olive_stairs",
			() -> new StairBlock(OLIVE_PLANKS.get().defaultBlockState(), planksProps(MapColor.TERRACOTTA_WHITE)));
	public static final DeferredBlock<StairBlock> IRONWOOD_STAIRS = BLOCKS.register("ironwood_stairs",
			() -> new StairBlock(IRONWOOD_PLANKS.get().defaultBlockState(), planksProps(MapColor.COLOR_GRAY)));
	public static final DeferredBlock<DoorBlock> OLIVE_DOOR = BLOCKS.registerBlock("olive_door", p -> new DoorBlock(BlockSetType.OAK, p),
			planksProps(MapColor.TERRACOTTA_WHITE).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
	public static final DeferredBlock<DoorBlock> IRONWOOD_DOOR = BLOCKS.registerBlock("ironwood_door", p -> new DoorBlock(BlockSetType.OAK, p),
			planksProps(MapColor.COLOR_GRAY).strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
	/** PR #252 (Quark-style carved wood) */
	public static final DeferredBlock<Block> CARVED_OLIVE_WOOD = BLOCKS.registerSimpleBlock("carved_olive_wood", planksProps(MapColor.TERRACOTTA_WHITE));
	/** PR #252 (Quark-style carved wood) */
	public static final DeferredBlock<Block> CARVED_IRONWOOD = BLOCKS.registerSimpleBlock("carved_ironwood", planksProps(MapColor.COLOR_GRAY));

	// ================================================================ apple trees

	public static final DeferredBlock<AppleSeedsBlock> APPLE_SEEDS = BLOCKS.registerBlock("apple_seeds", AppleSeedsBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP)
					.pushReaction(PushReaction.DESTROY));
	public static final DeferredBlock<RusticSaplingBlock> APPLE_SAPLING = BLOCKS.registerBlock("apple_sapling",
			p -> new RusticSaplingBlock(ModTreeGrowers.APPLE, p), saplingProps());
	public static final DeferredBlock<AppleLeavesBlock> APPLE_LEAVES = BLOCKS.registerBlock("apple_leaves", AppleLeavesBlock::new, leavesProps());

	// ================================================================ agriculture

	public static final DeferredBlock<FertileSoilBlock> FERTILE_SOIL = BLOCKS.registerBlock("fertile_soil", FertileSoilBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL));
	public static final DeferredBlock<CropStakeBlock> CROP_STAKE = BLOCKS.registerBlock("crop_stake", CropStakeBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 5.0F).sound(SoundType.WOOD).noOcclusion().forceSolidOff().ignitedByLava());
	public static final DeferredBlock<StakeTiedBlock> STAKE_TIED = BLOCKS.registerBlock("stake_tied", StakeTiedBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 5.0F).sound(SoundType.WOOD).noOcclusion().forceSolidOff().ignitedByLava());
	public static final DeferredBlock<StakeCropBlock> TOMATO_CROP = BLOCKS.registerBlock("tomato_crop",
			p -> new StakeCropBlock(p, ModItems.TOMATO_SEEDS, ModItems.TOMATO, null, 0, 3), stakeCropProps());
	public static final DeferredBlock<StakeCropBlock> CHILI_CROP = BLOCKS.registerBlock("chili_crop",
			p -> new StakeCropBlock(p, ModItems.CHILI_PEPPER_SEEDS, ModItems.CHILI_PEPPER, ModItems.GHOST_PEPPER, 42, 2), stakeCropProps());
	public static final DeferredBlock<BerryBushBlock> WILDBERRY_BUSH = BLOCKS.registerBlock("wildberry_bush",
			p -> new BerryBushBlock(p, ModItems.WILDBERRIES), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks()
					.strength(0.2F).sound(SoundType.SWEET_BERRY_BUSH).noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava());
	public static final DeferredBlock<GrapeStemBlock> GRAPE_STEM = BLOCKS.registerBlock("grape_stem", GrapeStemBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().strength(0.5F).sound(SoundType.CROP).noOcclusion().forceSolidOff()
					.pushReaction(PushReaction.DESTROY).ignitedByLava());
	public static final DeferredBlock<GrapeLeavesBlock> GRAPE_LEAVES = BLOCKS.registerBlock("grape_leaves", GrapeLeavesBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().strength(0.2F).sound(SoundType.GRASS).noOcclusion()
					.isSuffocating((s, l, p) -> false).isViewBlocking((s, l, p) -> false).pushReaction(PushReaction.DESTROY).ignitedByLava());

	// ================================================================ herbs

	public static final DeferredBlock<HerbBlock> ALOE_VERA = herb("aloe_vera", HerbBlock.PlantType.DESERT, 0);
	public static final DeferredBlock<HerbBlock> BLOOD_ORCHID = herb("blood_orchid", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> CHAMOMILE = herb("chamomile", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> CLOUDSBLUFF = herb("cloudsbluff", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> COHOSH = herb("cohosh", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> CORE_ROOT = herb("core_root", HerbBlock.PlantType.CAVE, 0);
	public static final DeferredBlock<HerbBlock> DEATHSTALK_MUSHROOM = herb("deathstalk_mushroom", HerbBlock.PlantType.NETHER, 0);
	public static final DeferredBlock<HerbBlock> GINSENG = herb("ginseng", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> HORSETAIL = herb("horsetail", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> MARSH_MALLOW = herb("marsh_mallow", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> MOONCAP_MUSHROOM = herb("mooncap_mushroom", HerbBlock.PlantType.CAVE, 8);
	public static final DeferredBlock<HerbBlock> WIND_THISTLE = herb("wind_thistle", HerbBlock.PlantType.PLAINS, 0);
	public static final DeferredBlock<HerbBlock> VANTA_LILY = herb("vanta_lily", HerbBlock.PlantType.PLAINS, 0);

	// ================================================================ storage & bees

	public static final DeferredBlock<VaseBlock> VASE = BLOCKS.registerBlock("vase", VaseBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).strength(0.5F).sound(SoundType.DECORATED_POT).noOcclusion());
	public static final DeferredBlock<RusticBarrelBlock> BARREL = BLOCKS.registerBlock("barrel", RusticBarrelBlock::new, woodMachineProps(1.5F));
	public static final DeferredBlock<CabinetBlock> CABINET = BLOCKS.registerBlock("cabinet", CabinetBlock::new, woodMachineProps(2.5F).noOcclusion());
	public static final DeferredBlock<ApiaryBlock> APIARY = BLOCKS.registerBlock("apiary", ApiaryBlock::new, woodMachineProps(1.0F).noOcclusion());
	public static final DeferredBlock<RusticBeehiveBlock> BEEHIVE = BLOCKS.registerBlock("beehive", RusticBeehiveBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.5F).sound(SoundType.HONEY_BLOCK).noOcclusion()
					.ignitedByLava());

	// ================================================================ alchemy & brewing

	public static final DeferredBlock<CondenserBlock> CONDENSER = BLOCKS.registerBlock("condenser", CondenserBlock::new, stoneMachineProps(2.0F));
	public static final DeferredBlock<RetortBlock> RETORT = BLOCKS.registerBlock("retort", RetortBlock::new, stoneMachineProps(2.0F));
	public static final DeferredBlock<AdvancedCondenserBlock> CONDENSER_ADVANCED = BLOCKS.registerBlock("condenser_advanced", AdvancedCondenserBlock::new,
			stoneMachineProps(2.0F));
	public static final DeferredBlock<RetortBlock> RETORT_ADVANCED = BLOCKS.registerBlock("retort_advanced", RetortBlock::new, stoneMachineProps(2.0F));
	public static final DeferredBlock<BrewingBarrelBlock> BREWING_BARREL = BLOCKS.registerBlock("brewing_barrel", BrewingBarrelBlock::new,
			woodMachineProps(1.5F).noOcclusion());
	public static final DeferredBlock<CrushingTubBlock> CRUSHING_TUB = BLOCKS.registerBlock("crushing_tub", CrushingTubBlock::new,
			woodMachineProps(1.5F).noOcclusion());
	public static final DeferredBlock<EvaporatingBasinBlock> EVAPORATING_BASIN = BLOCKS.registerBlock("evaporating_basin", EvaporatingBasinBlock::new,
			stoneMachineProps(1.25F));
	public static final DeferredBlock<LiquidBarrelBlock> LIQUID_BARREL = BLOCKS.registerBlock("liquid_barrel", LiquidBarrelBlock::new,
			woodMachineProps(1.5F));

	private ModBlocks() {
	}

	// ================================================================ helpers

	private static BlockBehaviour.Properties stoneProps() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops().strength(2.0F, 6.0F);
	}

	private static DeferredBlock<Block> stone(String name) {
		return BLOCKS.registerSimpleBlock(name, stoneProps());
	}

	private static DeferredBlock<RotatedPillarBlock> pillar(String name, MapColor color) {
		return BLOCKS.registerBlock(name, RotatedPillarBlock::new, stoneProps().mapColor(color));
	}

	private static BlockBehaviour.Properties clayProps() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(1.0F).sound(SoundType.GRAVEL);
	}

	private static BlockBehaviour.Properties candleProps() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noCollission().strength(1.0F).lightLevel(s -> 15)
				.sound(SoundType.METAL).pushReaction(PushReaction.DESTROY);
	}

	private static DeferredBlock<RusticChainBlock> chain(String name) {
		return BLOCKS.registerBlock(name, RusticChainBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
				.strength(1.0F, 6.0F).sound(SoundType.CHAIN).noOcclusion());
	}

	private static DeferredBlock<ChandelierBlock> chandelier(String name) {
		return BLOCKS.registerBlock(name, ChandelierBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
				.strength(2.0F, 6.0F).sound(SoundType.ANVIL).noOcclusion());
	}

	private static DeferredBlock<RusticLanternBlock> lantern(String name) {
		return BLOCKS.registerBlock(name, RusticLanternBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
				.strength(2.0F).lightLevel(s -> 15).sound(SoundType.LANTERN).noOcclusion().pushReaction(PushReaction.DESTROY));
	}

	private static BlockBehaviour.Properties furnitureProps(WoodVariant wood) {
		BlockBehaviour.Properties props = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(wood.sound()).noOcclusion();
		return wood.isFlammable() ? props.ignitedByLava() : props;
	}

	private static BlockBehaviour.Properties planksProps(MapColor color) {
		return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0F, 5.0F)
				.sound(SoundType.WOOD).ignitedByLava();
	}

	private static BlockBehaviour.Properties logProps(MapColor color) {
		return BlockBehaviour.Properties.of().mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD)
				.ignitedByLava();
	}

	private static BlockBehaviour.Properties leavesProps() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES);
	}

	private static BlockBehaviour.Properties saplingProps() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING);
	}

	private static BlockBehaviour.Properties stakeCropProps() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().instabreak().sound(SoundType.CROP).noOcclusion().forceSolidOff()
				.pushReaction(PushReaction.DESTROY);
	}

	private static DeferredBlock<HerbBlock> herb(String name, HerbBlock.PlantType type, int light) {
		return BLOCKS.registerBlock(name, p -> new HerbBlock(p, type), BlockBehaviour.Properties.of()
				.mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(SoundType.CROP).offsetType(BlockBehaviour.OffsetType.XZ)
				.lightLevel(s -> light).pushReaction(PushReaction.DESTROY).ignitedByLava());
	}

	private static BlockBehaviour.Properties woodMachineProps(float hardness) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(hardness)
				.sound(SoundType.WOOD).ignitedByLava();
	}

	private static BlockBehaviour.Properties stoneMachineProps(float hardness) {
		return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
				.strength(hardness, 6.0F).noOcclusion();
	}
}
