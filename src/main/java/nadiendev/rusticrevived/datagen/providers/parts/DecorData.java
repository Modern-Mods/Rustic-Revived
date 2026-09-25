package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.List;
import java.util.Map;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.block.WoodVariant;
import nadiendev.rusticrevived.block.decor.CandleLeverBlock;
import nadiendev.rusticrevived.block.decor.DoubleCandleBlock;
import nadiendev.rusticrevived.block.decor.LatticeBlock;
import nadiendev.rusticrevived.block.decor.MountedLightBlock;
import nadiendev.rusticrevived.block.decor.RopeBaseBlock;
import nadiendev.rusticrevived.block.decor.RusticLanternBlock;
import nadiendev.rusticrevived.block.decor.TableBlock;
import nadiendev.rusticrevived.datagen.providers.RusticBlockLoot;
import nadiendev.rusticrevived.datagen.providers.RusticBlockStateProvider;
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.recipe.ConfigCondition;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.NotCondition;
import net.neoforged.neoforge.common.conditions.TagEmptyCondition;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

/**
 * Data generation for the decoration (stone, painted wood, metal decor, furniture, rope) subsystem.
 */
public final class DecorData {
	/** Burn time of wooden blocks (legacy: every Material.WOOD block item burned like planks). */
	private static final int WOOD_BURN_TIME = 300;

	private DecorData() {
	}

	// ================================================================ block lists

	private static List<Block> pillars() {
		return List.of(ModBlocks.STONE_PILLAR.get(), ModBlocks.ANDESITE_PILLAR.get(), ModBlocks.DIORITE_PILLAR.get(), ModBlocks.GRANITE_PILLAR.get(),
				ModBlocks.SLATE_PILLAR.get(), ModBlocks.BASALT_PILLAR.get(), ModBlocks.LIMESTONE_PILLAR.get(), ModBlocks.MARBLE_PILLAR.get());
	}

	private static List<Block> slateBlocks() {
		return List.of(ModBlocks.SLATE.get(), ModBlocks.SLATE_ROOF.get(), ModBlocks.SLATE_TILE.get(), ModBlocks.SLATE_BRICK.get(),
				ModBlocks.SLATE_CHISELED.get(), ModBlocks.SLATE_PAVEMENT.get());
	}

	private static List<Block> chains() {
		return List.of(ModBlocks.CHAIN.get(), ModBlocks.CHAIN_GOLD.get(), ModBlocks.CHAIN_SILVER.get());
	}

	/** Metal decoration mined with a pickaxe. */
	private static List<Block> metalDecor() {
		return List.of(ModBlocks.CHAIN.get(), ModBlocks.CHAIN_GOLD.get(), ModBlocks.CHAIN_SILVER.get(),
				ModBlocks.CANDLE.get(), ModBlocks.CANDLE_GOLD.get(), ModBlocks.CANDLE_SILVER.get(),
				ModBlocks.CANDLE_DOUBLE.get(), ModBlocks.CANDLE_DOUBLE_GOLD.get(), ModBlocks.CANDLE_DOUBLE_SILVER.get(),
				ModBlocks.CANDLE_LEVER.get(), ModBlocks.CANDLE_LEVER_GOLD.get(), ModBlocks.CANDLE_LEVER_SILVER.get(),
				ModBlocks.CHANDELIER.get(), ModBlocks.CHANDELIER_GOLD.get(), ModBlocks.CHANDELIER_SILVER.get(),
				ModBlocks.IRON_LANTERN.get(), ModBlocks.GOLDEN_LANTERN.get(), ModBlocks.SILVER_LANTERN.get(), ModBlocks.IRON_LATTICE.get());
	}

	private static List<Block> all(Map<?, ? extends DeferredBlock<? extends Block>> blocks) {
		return blocks.values().stream().<Block>map(DeferredBlock::get).toList();
	}

	/** Texture of the painted wood of a color (light gray keeps its legacy "silver" texture). */
	private static String paintedWoodTexture(DyeColor color) {
		return "painted_wood_" + (color == DyeColor.LIGHT_GRAY ? "silver" : color.getSerializedName());
	}

	// ================================================================ block states and models

	public static void blockStates(RusticBlockStateProvider p) {
		stoneStates(p);
		metalStates(p);
		furnitureStates(p);
	}

	private static void stoneStates(RusticBlockStateProvider p) {
		pillar(p, ModBlocks.STONE_PILLAR.get(), "pillar_stone_side", p.blockTex("pillar_stone_top"));
		pillar(p, ModBlocks.ANDESITE_PILLAR.get(), "pillar_andesite_side", p.mcLoc("block/polished_andesite"));
		pillar(p, ModBlocks.DIORITE_PILLAR.get(), "pillar_diorite_side", p.mcLoc("block/polished_diorite"));
		pillar(p, ModBlocks.GRANITE_PILLAR.get(), "pillar_granite_side", p.mcLoc("block/polished_granite"));
		pillar(p, ModBlocks.SLATE_PILLAR.get(), "slate_pillar", p.blockTex("slate_tile"));
		pillar(p, ModBlocks.BASALT_PILLAR.get(), "pillar_basalt_side", p.mcLoc("block/polished_basalt_top"));
		pillar(p, ModBlocks.LIMESTONE_PILLAR.get(), "pillar_limestone_side", p.blockTex("pillar_limestone_top"));
		pillar(p, ModBlocks.MARBLE_PILLAR.get(), "pillar_marble_side", p.blockTex("pillar_marble_top"));

		for (Block block : slateBlocks()) {
			cube(p, block, p.name(block));
		}
		p.stairsBlock(ModBlocks.SLATE_ROOF_STAIRS.get(), p.blockTex("slate_roof"));
		p.stairsBlock(ModBlocks.SLATE_BRICK_STAIRS.get(), p.blockTex("slate_brick"));
		p.slabBlock(ModBlocks.SLATE_ROOF_SLAB.get(), RusticRevived.id("block/slate_roof"), p.blockTex("slate_roof"));
		p.slabBlock(ModBlocks.SLATE_BRICK_SLAB.get(), RusticRevived.id("block/slate_brick"), p.blockTex("slate_brick"));
		for (Block block : List.of(ModBlocks.SLATE_ROOF_STAIRS.get(), ModBlocks.SLATE_BRICK_STAIRS.get(), ModBlocks.SLATE_ROOF_SLAB.get(),
				ModBlocks.SLATE_BRICK_SLAB.get())) {
			p.blockItem(block);
		}

		cube(p, ModBlocks.CLAY_WALL.get(), "clay_wall");
		cube(p, ModBlocks.CLAY_WALL_CROSS.get(), "clay_wall_cross");
		p.horizontalBlock(ModBlocks.CLAY_WALL_DIAG.get(), existing(p, "clay_wall_diag"));
		p.blockItem(ModBlocks.CLAY_WALL_DIAG.get());
		p.horizontalBlock(ModBlocks.GARGOYLE.get(), existing(p, "gargoyle"), 0);
		p.blockItem(ModBlocks.GARGOYLE.get());

		for (DyeColor color : DyeColor.values()) {
			cube(p, ModBlocks.PAINTED_WOOD.get(color).get(), paintedWoodTexture(color));
		}
	}

	private static void metalStates(RusticBlockStateProvider p) {
		String[] metals = { "", "_gold", "_silver" };
		List<List<? extends Block>> byMetal = List.of(
				List.of(ModBlocks.CHAIN.get(), ModBlocks.CANDLE.get(), ModBlocks.CANDLE_DOUBLE.get(), ModBlocks.CANDLE_LEVER.get(),
						ModBlocks.CHANDELIER.get(), ModBlocks.IRON_LANTERN.get()),
				List.of(ModBlocks.CHAIN_GOLD.get(), ModBlocks.CANDLE_GOLD.get(), ModBlocks.CANDLE_DOUBLE_GOLD.get(), ModBlocks.CANDLE_LEVER_GOLD.get(),
						ModBlocks.CHANDELIER_GOLD.get(), ModBlocks.GOLDEN_LANTERN.get()),
				List.of(ModBlocks.CHAIN_SILVER.get(), ModBlocks.CANDLE_SILVER.get(), ModBlocks.CANDLE_DOUBLE_SILVER.get(),
						ModBlocks.CANDLE_LEVER_SILVER.get(), ModBlocks.CHANDELIER_SILVER.get(), ModBlocks.SILVER_LANTERN.get()));
		for (int i = 0; i < metals.length; i++) {
			String metal = metals[i];
			List<? extends Block> blocks = byMetal.get(i);
			Block chain = blocks.get(0);
			line(p, chain, existing(p, "chain" + metal), existing(p, "chain_horizontal" + metal), List.of(existing(p, "chain_dangle" + metal)));
			p.generatedItem(p.name(chain), "block/chain" + metal);

			Block candle = blocks.get(1);
			mountedLight(p, candle, existing(p, "candle" + metal), existing(p, "candle_wall" + metal), 0);
			p.blockItem(candle, "candle" + metal);
			doubleCandle(p, blocks.get(2), existing(p, "candle_double" + metal), existing(p, "candle_double_wall" + metal));
			p.blockItem(blocks.get(2), "candle_double" + metal);
			candleLever(p, blocks.get(3), metal);
			p.blockItem(blocks.get(3), "candle" + metal);

			p.simpleBlock(blocks.get(4), existing(p, "chandelier" + metal));
			p.blockItem(blocks.get(4), "chandelier" + metal);
			lantern(p, blocks.get(5), metal);
			p.blockItem(blocks.get(5), "lantern_wall" + metal);
		}

		mountedLight(p, ModBlocks.IRON_TORCH.get(), existing(p, "iron_torch_alt"), existing(p, "iron_torch_wall"), 270);
		p.blockItem(ModBlocks.IRON_TORCH.get(), "iron_torch_alt");
		mountedLight(p, ModBlocks.LANTERN_WOOD.get(), existing(p, "lantern_wood"), existing(p, "lantern_wood_wall"), 0);
		p.blockItem(ModBlocks.LANTERN_WOOD.get(), "lantern_wood");

		line(p, ModBlocks.ROPE.get(), existing(p, "rope"), existing(p, "rope_horizontal"), List.of(existing(p, "rope_dangle"), existing(p, "rope_knot")));
		p.generatedItem("rope", "block/rope");

		lattice(p);
		p.blockItem(ModBlocks.IRON_LATTICE.get(), "lattice_iron_inventory");
	}

	private static void furnitureStates(RusticBlockStateProvider p) {
		for (WoodVariant wood : WoodVariant.values()) {
			String name = wood.getSerializedName();
			ResourceLocation planks = wood.planksTexture();

			Block chair = ModBlocks.CHAIRS.get(wood).get();
			p.horizontalBlock(chair, p.models().withExistingParent("chair_" + name, RusticRevived.id("block/chair_template")).texture("planks", planks));
			p.blockItem(chair);

			Block table = ModBlocks.TABLES.get(wood).get();
			ModelFile top = p.models().withExistingParent("table_" + name + "_top", RusticRevived.id("block/table_top_template")).texture("planks", planks);
			ModelFile leg = p.models().withExistingParent("table_" + name + "_leg", RusticRevived.id("block/table_leg_template")).texture("planks", planks);
			p.models().withExistingParent("table_" + name, RusticRevived.id("block/table_template")).texture("planks", planks);
			MultiPartBlockStateBuilder builder = p.getMultipartBuilder(table).part().modelFile(top).addModel().end();
			BooleanProperty[] corners = { TableBlock.NW, TableBlock.NE, TableBlock.SE, TableBlock.SW };
			for (int i = 0; i < corners.length; i++) {
				builder.part().modelFile(leg).rotationY(90 * i).addModel().condition(corners[i], true).end();
			}
			p.blockItem(table);
		}
	}

	private static ModelFile existing(RusticBlockStateProvider p, String model) {
		return p.models().getExistingFile(RusticRevived.id("block/" + model));
	}

	private static void cube(RusticBlockStateProvider p, Block block, String texture) {
		p.simpleBlock(block, p.models().cubeAll(p.name(block), p.blockTex(texture)));
		p.blockItem(block);
	}

	private static void pillar(RusticBlockStateProvider p, RotatedPillarBlock block, String side, ResourceLocation end) {
		p.axisBlock(block, p.blockTex(side), end);
		p.blockItem(block);
	}

	/** Rope or chain: vertical or horizontal line plus the dangling parts of horizontal lines (legacy multipart). */
	private static void line(RusticBlockStateProvider p, Block block, ModelFile vertical, ModelFile horizontal, List<ModelFile> dangle) {
		MultiPartBlockStateBuilder builder = p.getMultipartBuilder(block)
				.part().modelFile(vertical).addModel().condition(RopeBaseBlock.AXIS, Direction.Axis.Y).end()
				.part().modelFile(horizontal).addModel().condition(RopeBaseBlock.AXIS, Direction.Axis.Z).end()
				.part().modelFile(horizontal).rotationY(90).addModel().condition(RopeBaseBlock.AXIS, Direction.Axis.X).end();
		for (ModelFile model : dangle) {
			builder.part().modelFile(model).addModel().condition(RopeBaseBlock.AXIS, Direction.Axis.Z).condition(RopeBaseBlock.DANGLE, true).end()
					.part().modelFile(model).rotationY(90).addModel().condition(RopeBaseBlock.AXIS, Direction.Axis.X).condition(RopeBaseBlock.DANGLE, true)
					.end();
		}
	}

	/** Candle, iron torch or wood lantern; {@code wallOffset} is the model rotation of {@code facing=north}. */
	private static void mountedLight(RusticBlockStateProvider p, Block block, ModelFile standing, ModelFile wall, int wallOffset) {
		p.getVariantBuilder(block).forAllStates(state -> {
			Direction facing = state.getValue(MountedLightBlock.FACING);
			return facing == Direction.UP ? ConfiguredModel.builder().modelFile(standing).build()
					: ConfiguredModel.builder().modelFile(wall).rotationY(wallRotation(facing, wallOffset)).build();
		});
	}

	private static void candleLever(RusticBlockStateProvider p, Block block, String metal) {
		ModelFile standing = existing(p, "candle" + metal);
		ModelFile wall = existing(p, "candle_wall" + metal);
		ModelFile standingPulled = existing(p, "candle_pulled" + metal);
		ModelFile wallPulled = existing(p, "candle_wall_pulled" + metal);
		p.getVariantBuilder(block).forAllStates(state -> {
			Direction facing = state.getValue(MountedLightBlock.FACING);
			boolean pulled = state.getValue(CandleLeverBlock.POWERED);
			return facing == Direction.UP ? ConfiguredModel.builder().modelFile(pulled ? standingPulled : standing).build()
					: ConfiguredModel.builder().modelFile(pulled ? wallPulled : wall).rotationY(wallRotation(facing, 0)).build();
		});
	}

	private static void doubleCandle(RusticBlockStateProvider p, Block block, ModelFile standing, ModelFile wall) {
		p.getVariantBuilder(block).forAllStates(state -> {
			DoubleCandleBlock.Mount mount = state.getValue(DoubleCandleBlock.FACING);
			return switch (mount) {
				case UP_X -> ConfiguredModel.builder().modelFile(standing).rotationY(90).build();
				case UP_Z -> ConfiguredModel.builder().modelFile(standing).build();
				default -> ConfiguredModel.builder().modelFile(wall).rotationY(wallRotation(mount.facing(), 0)).build();
			};
		});
	}

	private static void lantern(RusticBlockStateProvider p, Block block, String metal) {
		ModelFile up = existing(p, "lantern_up" + metal);
		ModelFile down = existing(p, "lantern_down" + metal);
		ModelFile wall = existing(p, "lantern_wall" + metal);
		p.getVariantBuilder(block).forAllStates(state -> {
			Direction facing = state.getValue(RusticLanternBlock.FACING);
			return switch (facing) {
				case UP -> ConfiguredModel.builder().modelFile(up).build();
				case DOWN -> ConfiguredModel.builder().modelFile(down).build();
				default -> ConfiguredModel.builder().modelFile(wall).rotationY(wallRotation(facing, 0)).build();
			};
		});
	}

	/** Y rotation of a wall model whose unrotated model is the {@code facing=north} one rotated by {@code northRotation}. */
	private static int wallRotation(Direction facing, int northRotation) {
		return ((int) facing.toYRot() + 180 + northRotation) % 360;
	}

	/** Core, bars towards every connection, and the same with leaves when the lattice is leafy (legacy LatticeModel). */
	private static void lattice(RusticBlockStateProvider p) {
		ModelFile core = existing(p, "lattice_iron_base");
		ModelFile bar = existing(p, "lattice_iron_bar");
		ModelFile leavesCore = existing(p, "lattice_leaves_base");
		ModelFile leavesBar = existing(p, "lattice_leaves_bar");
		MultiPartBlockStateBuilder builder = p.getMultipartBuilder(ModBlocks.IRON_LATTICE.get())
				.part().modelFile(core).addModel().end()
				.part().modelFile(leavesCore).addModel().condition(LatticeBlock.LEAVES, true).end();
		for (Direction direction : Direction.values()) {
			int x = direction == Direction.DOWN ? 90 : direction == Direction.UP ? 270 : 0;
			int y = direction.getAxis().isHorizontal() ? wallRotation(direction, 0) : 0;
			BooleanProperty connection = LatticeBlock.CONNECTIONS.get(direction);
			builder.part().modelFile(bar).rotationX(x).rotationY(y).addModel().condition(connection, true).end()
					.part().modelFile(leavesBar).rotationX(x).rotationY(y).addModel().condition(connection, true).condition(LatticeBlock.LEAVES, true).end();
		}
	}

	// ================================================================ recipes

	public static void recipes(RusticRecipeProvider p, RecipeOutput out) {
		stoneRecipes(out);
		woodRecipes(out);
		metalRecipes(out);
	}

	private static void stoneRecipes(RecipeOutput out) {
		RecipeOutput pillars = RusticRecipeProvider.whenConfig(out, "enablePillars");
		pillar(pillars, "pillar_stone", ModBlocks.STONE_PILLAR.get(), Ingredient.of(Blocks.STONE), RusticRecipeProvider.hasItem(Blocks.STONE));
		pillar(pillars, "pillar_andesite", ModBlocks.ANDESITE_PILLAR.get(), Ingredient.of(Blocks.ANDESITE), RusticRecipeProvider.hasItem(Blocks.ANDESITE));
		pillar(pillars, "pillar_diorite", ModBlocks.DIORITE_PILLAR.get(), Ingredient.of(Blocks.DIORITE), RusticRecipeProvider.hasItem(Blocks.DIORITE));
		pillar(pillars, "pillar_granite", ModBlocks.GRANITE_PILLAR.get(), Ingredient.of(Blocks.GRANITE), RusticRecipeProvider.hasItem(Blocks.GRANITE));
		pillar(pillars, "pillar_basalt", ModBlocks.BASALT_PILLAR.get(), Ingredient.of(Blocks.POLISHED_BASALT),
				RusticRecipeProvider.hasItem(Blocks.POLISHED_BASALT));
		pillar(withTag(out, "enablePillars", ModTags.Items.LIMESTONE_STONES), "pillar_limestone", ModBlocks.LIMESTONE_PILLAR.get(),
				Ingredient.of(ModTags.Items.LIMESTONE_STONES), RusticRecipeProvider.hasTag(ModTags.Items.LIMESTONE_STONES));
		pillar(withTag(out, "enablePillars", ModTags.Items.MARBLE_STONES), "pillar_marble", ModBlocks.MARBLE_PILLAR.get(),
				Ingredient.of(ModTags.Items.MARBLE_STONES), RusticRecipeProvider.hasTag(ModTags.Items.MARBLE_STONES));
		RecipeOutput slatePillars = out.withConditions(ConfigCondition.of("enablePillars"), ConfigCondition.of("enableSlate"));
		pillar(slatePillars, "pillar_slate", ModBlocks.SLATE_PILLAR.get(), Ingredient.of(ModBlocks.SLATE), RusticRecipeProvider.hasItem(ModBlocks.SLATE));
		stonecutting(slatePillars, ModBlocks.SLATE.get(), ModBlocks.SLATE_PILLAR.get(), 1);

		RecipeOutput slate = RusticRecipeProvider.whenConfig(out, "enableSlate");
		ItemLike slateItem = ModBlocks.SLATE.get();
		shaped(slate, "slate_roof", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_ROOF.get(), 4, slateItem, "SS", "SS");
		shaped(slate, "slate_roof_stairs", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_ROOF_STAIRS.get(), 4, ModBlocks.SLATE_ROOF.get(),
				"S  ", "SS ", "SSS");
		shaped(slate, "slate_roof_slab", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_ROOF_SLAB.get(), 6, ModBlocks.SLATE_ROOF.get(), "SSS");
		shaped(slate, "slate_brick_stairs", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_BRICK_STAIRS.get(), 4, ModBlocks.SLATE_BRICK.get(),
				"S  ", "SS ", "SSS");
		shaped(slate, "slate_brick_slab", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_BRICK_SLAB.get(), 6, ModBlocks.SLATE_BRICK.get(), "SSS");
		shaped(slate, "slate_tile", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_TILE.get(), 1, slateItem, "S");
		shaped(slate, "slate_tile_from_slate_brick", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_TILE.get(), 1, ModBlocks.SLATE_BRICK.get(), "S");
		shaped(slate, "slate_brick", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_BRICK.get(), 4, ModBlocks.SLATE_TILE.get(), "SS", "SS");
		shaped(slate, "slate_chiseled", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_CHISELED.get(), 4, ModBlocks.SLATE_BRICK.get(), "SS", "SS");
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_PAVEMENT.get(), 4)
				.pattern("SB").pattern("BS").define('S', slateItem).define('B', ModBlocks.SLATE_BRICK.get())
				.unlockedBy(RusticRecipeProvider.hasName(slateItem), RusticRecipeProvider.hasItem(slateItem))
				.save(slate, RusticRevived.id("slate_pavement"));

		for (Block result : List.of(ModBlocks.SLATE_ROOF.get(), ModBlocks.SLATE_TILE.get(), ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_CHISELED.get(),
				ModBlocks.SLATE_PAVEMENT.get(), ModBlocks.SLATE_ROOF_STAIRS.get(), ModBlocks.SLATE_BRICK_STAIRS.get())) {
			stonecutting(slate, ModBlocks.SLATE.get(), result, 1);
		}
		stonecutting(slate, ModBlocks.SLATE.get(), ModBlocks.SLATE_ROOF_SLAB.get(), 2);
		stonecutting(slate, ModBlocks.SLATE.get(), ModBlocks.SLATE_BRICK_SLAB.get(), 2);
		stonecutting(slate, ModBlocks.SLATE_ROOF.get(), ModBlocks.SLATE_ROOF_STAIRS.get(), 1);
		stonecutting(slate, ModBlocks.SLATE_ROOF.get(), ModBlocks.SLATE_ROOF_SLAB.get(), 2);
		stonecutting(slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_BRICK_STAIRS.get(), 1);
		stonecutting(slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_BRICK_SLAB.get(), 2);
		stonecutting(slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_CHISELED.get(), 1);
		stonecutting(slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_TILE.get(), 1);
		stonecutting(slate, ModBlocks.SLATE_TILE.get(), ModBlocks.SLATE_BRICK.get(), 1);

		RecipeOutput clayWalls = RusticRecipeProvider.whenConfig(out, "enableClayWalls");
		clayWall(clayWalls, ModBlocks.CLAY_WALL.get(), 8, " P ", "PCP", " P ");
		clayWall(clayWalls, ModBlocks.CLAY_WALL_CROSS.get(), 1, "P P", " C ", "P P");
		clayWall(clayWalls, ModBlocks.CLAY_WALL_DIAG.get(), 1, "P  ", " C ", "  P");

		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.GARGOYLE.get(), 2)
				.pattern("PRP").pattern("SSS")
				.define('P', Blocks.STONE_PRESSURE_PLATE).define('R', Blocks.STONE).define('S', Blocks.SMOOTH_STONE_SLAB)
				.unlockedBy(RusticRecipeProvider.hasName(Blocks.STONE), RusticRecipeProvider.hasItem(Blocks.STONE))
				.save(out, RusticRevived.id("gargoyle"));
	}

	/** Clay wall recipe: 'P' is any planks, 'C' clay for the plain wall and a clay wall for the beamed ones. */
	private static void clayWall(RecipeOutput out, Block result, int count, String... pattern) {
		ItemLike clay = result == ModBlocks.CLAY_WALL.get() ? Blocks.CLAY : ModBlocks.CLAY_WALL.get();
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, count).define('P', ItemTags.PLANKS).define('C', clay);
		for (String line : pattern) {
			builder.pattern(line);
		}
		builder.unlockedBy(RusticRecipeProvider.hasName(clay), RusticRecipeProvider.hasItem(clay)).save(out, RusticRevived.id(itemName(result)));
	}

	/** Recipe made of a single ingredient 'S'. */
	private static void shaped(RecipeOutput out, String id, RecipeCategory category, ItemLike result, int count, ItemLike input, String... pattern) {
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(category, result, count).define('S', input);
		for (String line : pattern) {
			builder.pattern(line);
		}
		builder.unlockedBy(RusticRecipeProvider.hasName(input), RusticRecipeProvider.hasItem(input)).save(out, RusticRevived.id(id));
	}

	private static void pillar(RecipeOutput out, String id, Block result, Ingredient stone, Criterion<?> unlock) {
		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 6).pattern("SS").pattern("SS").pattern("SS").define('S', stone)
				.unlockedBy("has_stone", unlock).save(out, RusticRevived.id(id));
	}

	private static void stonecutting(RecipeOutput out, ItemLike input, ItemLike result, int count) {
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count)
				.unlockedBy(RusticRecipeProvider.hasName(input), RusticRecipeProvider.hasItem(input))
				.save(out, RusticRevived.id(itemName(result) + "_from_" + itemName(input) + "_stonecutting"));
	}

	private static String itemName(ItemLike item) {
		return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
	}

	/** Output enabled by a config option and only loaded when some item has the (modded) tag. */
	private static RecipeOutput withTag(RecipeOutput out, String option, TagKey<Item> tag) {
		return out.withConditions(ConfigCondition.of(option), new NotCondition(new TagEmptyCondition(tag)));
	}

	private static void woodRecipes(RecipeOutput out) {
		RecipeOutput chairs = RusticRecipeProvider.whenConfig(out, "enableChairs");
		RecipeOutput tables = RusticRecipeProvider.whenConfig(out, "enableTables");
		for (WoodVariant wood : WoodVariant.values()) {
			Block planks = wood.planks();
			ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.CHAIRS.get(wood).get(), 4)
					.pattern("P  ").pattern("PPP").pattern("S S").define('P', planks).define('S', Tags.Items.RODS_WOODEN).group(RusticRevived.NAMESPACE + ":chair")
					.unlockedBy(RusticRecipeProvider.hasName(planks), RusticRecipeProvider.hasItem(planks))
					.save(chairs, RusticRevived.id(wood.getSerializedName() + "_chair"));
			ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TABLES.get(wood).get(), 2)
					.pattern("PPP").pattern("S S").define('P', planks).define('S', Tags.Items.RODS_WOODEN).group(RusticRevived.NAMESPACE + ":table")
					.unlockedBy(RusticRecipeProvider.hasName(planks), RusticRecipeProvider.hasItem(planks))
					.save(tables, RusticRevived.id(wood.getSerializedName() + "_table"));
		}

		RecipeOutput painted = RusticRecipeProvider.whenConfig(out, "enablePaintedWood");
		for (DyeColor color : DyeColor.values()) {
			ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PAINTED_WOOD.get(color).get(), 8)
					.pattern("PPP").pattern("PDP").pattern("PPP").define('P', ItemTags.PLANKS).define('D', color.getTag())
					.group(RusticRevived.NAMESPACE + ":painted_wood")
					.unlockedBy("has_dye", RusticRecipeProvider.hasTag(color.getTag()))
					.save(painted, RusticRevived.id("painted_wood_" + color.getSerializedName()));
		}

		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LANTERN_WOOD.get())
				.pattern("SPS").pattern("GTG").pattern("SPS")
				.define('S', Tags.Items.RODS_WOODEN).define('P', ItemTags.PLANKS).define('G', Tags.Items.GLASS_PANES_COLORLESS).define('T', Items.TORCH)
				.unlockedBy(RusticRecipeProvider.hasName(Items.TORCH), RusticRecipeProvider.hasItem(Items.TORCH))
				.save(out, RusticRevived.id("lantern_wood"));
	}

	private static void metalRecipes(RecipeOutput out) {
		metalSet(out, "", Tags.Items.INGOTS_IRON, ModBlocks.CANDLE.get(), ModBlocks.CANDLE_DOUBLE.get(), ModBlocks.CANDLE_LEVER.get(), ModBlocks.CHAIN.get(),
				ModBlocks.CHANDELIER.get(), ModBlocks.IRON_LANTERN.get(), "iron_lantern");
		metalSet(out, "_gold", Tags.Items.INGOTS_GOLD, ModBlocks.CANDLE_GOLD.get(), ModBlocks.CANDLE_DOUBLE_GOLD.get(), ModBlocks.CANDLE_LEVER_GOLD.get(),
				ModBlocks.CHAIN_GOLD.get(), ModBlocks.CHANDELIER_GOLD.get(), ModBlocks.GOLDEN_LANTERN.get(), "golden_lantern");
		ICondition silverExists = new NotCondition(new TagEmptyCondition(ModTags.Items.INGOTS_SILVER));
		metalSet(out.withConditions(ConfigCondition.of("enableSilverDecor"), silverExists), "_silver", ModTags.Items.INGOTS_SILVER,
				ModBlocks.CANDLE_SILVER.get(), ModBlocks.CANDLE_DOUBLE_SILVER.get(), ModBlocks.CANDLE_LEVER_SILVER.get(), ModBlocks.CHAIN_SILVER.get(),
				ModBlocks.CHANDELIER_SILVER.get(), ModBlocks.SILVER_LANTERN.get(), "silver_lantern");

		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.IRON_TORCH.get(), 4)
				.pattern("I").pattern("C").pattern("S")
				.define('I', Tags.Items.NUGGETS_IRON).define('C', ItemTags.COALS).define('S', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_iron_nugget", RusticRecipeProvider.hasTag(Tags.Items.NUGGETS_IRON))
				.save(out, RusticRevived.id("iron_torch"));
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.IRON_LATTICE.get(), 16)
				.pattern(" I ").pattern("III").pattern(" I ").define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", RusticRecipeProvider.hasTag(Tags.Items.INGOTS_IRON))
				.save(RusticRecipeProvider.whenConfig(out, "enableLattice"), RusticRevived.id("iron_lattice"));
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.ROPE.get(), 12)
				.pattern("S").pattern("S").pattern("S").define('S', Items.STRING)
				.unlockedBy(RusticRecipeProvider.hasName(Items.STRING), RusticRecipeProvider.hasItem(Items.STRING))
				.save(out, RusticRevived.id("rope"));
	}

	/** Candles, candle lever, chain, chandelier and lantern of one metal; recipe ids end with {@code suffix}. */
	private static void metalSet(RecipeOutput out, String suffix, TagKey<Item> ingot, Block candle, Block doubleCandle, Block lever, Block chain,
			Block chandelier, Block lantern, String lanternId) {
		String hasIngot = "has_ingot";
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, candle, 4)
				.pattern("S").pattern("W").pattern("I").define('S', Items.STRING).define('W', ModTags.Items.WAX).define('I', ingot)
				.unlockedBy(hasIngot, RusticRecipeProvider.hasTag(ingot)).save(out, RusticRevived.id("candle" + suffix));
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, doubleCandle, 4)
				.pattern("S S").pattern("W W").pattern(" I ").define('S', Items.STRING).define('W', ModTags.Items.WAX).define('I', ingot)
				.unlockedBy(hasIngot, RusticRecipeProvider.hasTag(ingot)).save(out, RusticRevived.id("candle_double" + suffix));
		ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, lever)
				.pattern("C").pattern("R").define('C', candle).define('R', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy(RusticRecipeProvider.hasName(candle), RusticRecipeProvider.hasItem(candle)).save(out, RusticRevived.id("candle_lever" + suffix));
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, chain, 12)
				.pattern("I").pattern("I").pattern("I").define('I', ingot)
				.unlockedBy(hasIngot, RusticRecipeProvider.hasTag(ingot)).save(out, RusticRevived.id("chain" + suffix));
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, chandelier, 2)
				.pattern(" I ").pattern("C C").pattern("III").define('I', ingot).define('C', chain)
				.unlockedBy(RusticRecipeProvider.hasName(chain), RusticRecipeProvider.hasItem(chain)).save(out, RusticRevived.id("chandelier" + suffix));
		ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, lantern, 4)
				.pattern("I").pattern("C").pattern("I").define('I', ingot).define('C', ItemTags.COALS)
				.unlockedBy(hasIngot, RusticRecipeProvider.hasTag(ingot)).save(out, RusticRevived.id(lanternId));
	}

	// ================================================================ loot tables

	public static void blockLoot(RusticBlockLoot l) {
		for (Block block : pillars()) {
			l.selfDrop(block);
		}
		for (Block block : slateBlocks()) {
			l.selfDrop(block);
		}
		l.selfDrop(ModBlocks.SLATE_ROOF_STAIRS.get());
		l.selfDrop(ModBlocks.SLATE_BRICK_STAIRS.get());
		l.table(ModBlocks.SLATE_ROOF_SLAB.get(), l.slab(ModBlocks.SLATE_ROOF_SLAB.get()));
		l.table(ModBlocks.SLATE_BRICK_SLAB.get(), l.slab(ModBlocks.SLATE_BRICK_SLAB.get()));
		l.selfDrop(ModBlocks.CLAY_WALL.get());
		l.selfDrop(ModBlocks.CLAY_WALL_CROSS.get());
		l.selfDrop(ModBlocks.CLAY_WALL_DIAG.get());
		l.selfDrop(ModBlocks.GARGOYLE.get());
		all(ModBlocks.PAINTED_WOOD).forEach(l::selfDrop);
		metalDecor().forEach(l::selfDrop);
		l.selfDrop(ModBlocks.IRON_TORCH.get());
		l.selfDrop(ModBlocks.LANTERN_WOOD.get());
		l.selfDrop(ModBlocks.ROPE.get());
		all(ModBlocks.CHAIRS).forEach(l::selfDrop);
		all(ModBlocks.TABLES).forEach(l::selfDrop);
	}

	// ================================================================ tags

	public static void blockTags(RusticTagProviders.Blocks t) {
		Block[] pillars = pillars().toArray(Block[]::new);
		Block[] slate = slateBlocks().toArray(Block[]::new);
		Block[] slateShapes = { ModBlocks.SLATE_ROOF_STAIRS.get(), ModBlocks.SLATE_BRICK_STAIRS.get(), ModBlocks.SLATE_ROOF_SLAB.get(),
				ModBlocks.SLATE_BRICK_SLAB.get() };
		Block[] paintedWood = all(ModBlocks.PAINTED_WOOD).toArray(Block[]::new);
		Block[] chairs = all(ModBlocks.CHAIRS).toArray(Block[]::new);
		Block[] tables = all(ModBlocks.TABLES).toArray(Block[]::new);

		t.tagOf(BlockTags.MINEABLE_WITH_PICKAXE).add(pillars).add(slate).add(slateShapes).add(ModBlocks.GARGOYLE.get()).add(metalDecor().toArray(Block[]::new))
				.add(ModBlocks.IRON_TORCH.get());
		t.tagOf(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.CLAY_WALL.get(), ModBlocks.CLAY_WALL_CROSS.get(), ModBlocks.CLAY_WALL_DIAG.get());
		t.tagOf(BlockTags.MINEABLE_WITH_AXE).add(paintedWood).add(chairs).add(tables).add(ModBlocks.LANTERN_WOOD.get());

		t.tagOf(ModTags.Blocks.PILLARS).add(pillars);
		t.tagOf(ModTags.Blocks.PAINTED_WOOD).add(paintedWood);
		t.tagOf(ModTags.Blocks.CHAIRS).add(chairs);
		t.tagOf(ModTags.Blocks.TABLES).add(tables);
		t.tagOf(ModTags.Blocks.SLATE_STONES).add(ModBlocks.SLATE.get());
		t.tagOf(ModTags.Blocks.CHANDELIER_SUPPORTS).add(Blocks.CHAIN, ModBlocks.IRON_LATTICE.get());
		t.tagOf(BlockTags.PLANKS).add(paintedWood);
		t.tagOf(BlockTags.STAIRS).add(ModBlocks.SLATE_ROOF_STAIRS.get(), ModBlocks.SLATE_BRICK_STAIRS.get());
		t.tagOf(BlockTags.SLABS).add(ModBlocks.SLATE_ROOF_SLAB.get(), ModBlocks.SLATE_BRICK_SLAB.get());
		t.tagOf(BlockTags.CLIMBABLE).add(ModBlocks.ROPE.get()).add(chains().toArray(Block[]::new));
		t.tagOf(Tags.Blocks.CHAINS).add(chains().toArray(Block[]::new));
		t.tagOf(Tags.Blocks.ROPES).add(ModBlocks.ROPE.get());
	}

	public static void itemTags(RusticTagProviders.Items t) {
		t.copyTag(ModTags.Blocks.PILLARS, ModTags.Items.PILLARS);
		t.copyTag(ModTags.Blocks.PAINTED_WOOD, ModTags.Items.PAINTED_WOOD);
		t.copyTag(ModTags.Blocks.CHAIRS, ModTags.Items.CHAIRS);
		t.copyTag(ModTags.Blocks.TABLES, ModTags.Items.TABLES);
		t.copyTag(ModTags.Blocks.SLATE_STONES, ModTags.Items.SLATE_STONES);
		t.copyTag(BlockTags.PLANKS, ItemTags.PLANKS);
		t.copyTag(BlockTags.STAIRS, ItemTags.STAIRS);
		t.copyTag(BlockTags.SLABS, ItemTags.SLABS);
		t.copyTag(Tags.Blocks.CHAINS, Tags.Items.CHAINS);
		t.copyTag(Tags.Blocks.ROPES, Tags.Items.ROPES);
	}

	// ================================================================ data maps

	public static void dataMaps(RusticDataMapProvider p) {
		var fuels = p.map(NeoForgeDataMaps.FURNACE_FUELS);
		FurnaceFuel wood = new FurnaceFuel(WOOD_BURN_TIME);
		ModBlocks.PAINTED_WOOD.values().forEach(block -> fuels.add(block.getId(), wood, false));
		for (WoodVariant variant : WoodVariant.values()) {
			if (variant.isFlammable()) {
				fuels.add(ModBlocks.CHAIRS.get(variant).getId(), wood, false);
				fuels.add(ModBlocks.TABLES.get(variant).getId(), wood, false);
			}
		}
		fuels.add(ModBlocks.LANTERN_WOOD.getId(), wood, false);
	}
}
