package nadiendev.rusticrevived.datagen.providers.parts;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.math.Quadrant;

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
import nadiendev.rusticrevived.datagen.providers.RusticDataMapProvider;
import nadiendev.rusticrevived.datagen.providers.RusticModelProvider;
import nadiendev.rusticrevived.datagen.providers.RusticRecipeProvider;
import nadiendev.rusticrevived.datagen.providers.RusticTagProviders;
import nadiendev.rusticrevived.recipe.ConfigCondition;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.advancements.Criterion;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
	/** Texture slot of the chair and table template models. */
	private static final TextureSlot PLANKS = TextureSlot.create("planks");
	private static final ModelTemplate CHAIR = template("chair_template");
	private static final ModelTemplate TABLE = template("table_template");
	private static final ModelTemplate TABLE_TOP = template("table_top_template");
	private static final ModelTemplate TABLE_LEG = template("table_leg_template");

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

	// ================================================================ block states, models and client items

	/** Block states, block models, item models and client item definitions. */
	public static void models(RusticModelProvider p, BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		stoneModels(blockModels);
		metalModels(blockModels);
		furnitureModels(blockModels);
	}

	private static void stoneModels(BlockModelGenerators g) {
		pillar(g, ModBlocks.STONE_PILLAR.get(), "pillar_stone_side", tex("pillar_stone_top"));
		pillar(g, ModBlocks.ANDESITE_PILLAR.get(), "pillar_andesite_side", mcTex("polished_andesite"));
		pillar(g, ModBlocks.DIORITE_PILLAR.get(), "pillar_diorite_side", mcTex("polished_diorite"));
		pillar(g, ModBlocks.GRANITE_PILLAR.get(), "pillar_granite_side", mcTex("polished_granite"));
		pillar(g, ModBlocks.SLATE_PILLAR.get(), "slate_pillar", tex("slate_tile"));
		pillar(g, ModBlocks.BASALT_PILLAR.get(), "pillar_basalt_side", mcTex("polished_basalt_top"));
		pillar(g, ModBlocks.LIMESTONE_PILLAR.get(), "pillar_limestone_side", tex("pillar_limestone_top"));
		pillar(g, ModBlocks.MARBLE_PILLAR.get(), "pillar_marble_side", tex("pillar_marble_top"));

		for (Block block : slateBlocks()) {
			cube(g, block, RusticModelProvider.name(block));
		}
		stairs(g, ModBlocks.SLATE_ROOF_STAIRS.get(), "slate_roof");
		stairs(g, ModBlocks.SLATE_BRICK_STAIRS.get(), "slate_brick");
		slab(g, ModBlocks.SLATE_ROOF_SLAB.get(), "slate_roof");
		slab(g, ModBlocks.SLATE_BRICK_SLAB.get(), "slate_brick");

		cube(g, ModBlocks.CLAY_WALL.get(), "clay_wall");
		cube(g, ModBlocks.CLAY_WALL_CROSS.get(), "clay_wall_cross");
		Identifier clayWallDiag = RusticModelProvider.existingBlockModel("clay_wall_diag");
		g.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.CLAY_WALL_DIAG.get(), BlockModelGenerators.plainVariant(clayWallDiag))
				.with(PropertyDispatch.modify(HorizontalDirectionalBlock.FACING).generate(facing -> yRot(wallRotation(facing, 0)))));
		g.registerSimpleItemModel(ModBlocks.CLAY_WALL_DIAG.get(), clayWallDiag);
		Identifier gargoyle = RusticModelProvider.existingBlockModel("gargoyle");
		g.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.GARGOYLE.get(), BlockModelGenerators.plainVariant(gargoyle))
				.with(PropertyDispatch.modify(HorizontalDirectionalBlock.FACING).generate(facing -> yRot((int) facing.toYRot() % 360))));
		g.registerSimpleItemModel(ModBlocks.GARGOYLE.get(), gargoyle);

		for (DyeColor color : DyeColor.values()) {
			cube(g, ModBlocks.PAINTED_WOOD.get(color).get(), paintedWoodTexture(color));
		}
	}

	private static void metalModels(BlockModelGenerators g) {
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
			line(g, chain, existing("chain" + metal), existing("chain_horizontal" + metal), List.of(existing("chain_dangle" + metal)));
			g.registerSimpleFlatItemModel(chain);

			Block candle = blocks.get(1);
			mountedLight(g, candle, existing("candle" + metal), existing("candle_wall" + metal), 0);
			g.registerSimpleItemModel(candle, existing("candle" + metal));
			doubleCandle(g, blocks.get(2), existing("candle_double" + metal), existing("candle_double_wall" + metal));
			g.registerSimpleItemModel(blocks.get(2), existing("candle_double" + metal));
			candleLever(g, blocks.get(3), metal);
			g.registerSimpleItemModel(blocks.get(3), existing("candle" + metal));

			Identifier chandelier = existing("chandelier" + metal);
			g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(blocks.get(4), BlockModelGenerators.plainVariant(chandelier)));
			g.registerSimpleItemModel(blocks.get(4), chandelier);
			lantern(g, blocks.get(5), metal);
			g.registerSimpleItemModel(blocks.get(5), existing("lantern_wall" + metal));
		}

		mountedLight(g, ModBlocks.IRON_TORCH.get(), existing("iron_torch_alt"), existing("iron_torch_wall"), 270);
		g.registerSimpleItemModel(ModBlocks.IRON_TORCH.get(), existing("iron_torch_alt"));
		mountedLight(g, ModBlocks.LANTERN_WOOD.get(), existing("lantern_wood"), existing("lantern_wood_wall"), 0);
		g.registerSimpleItemModel(ModBlocks.LANTERN_WOOD.get(), existing("lantern_wood"));

		line(g, ModBlocks.ROPE.get(), existing("rope"), existing("rope_horizontal"), List.of(existing("rope_dangle"), existing("rope_knot")));
		g.registerSimpleFlatItemModel(ModBlocks.ROPE.get());

		lattice(g);
		// tint index 1 is the foliage of leafy lattice (the inventory model is bare iron)
		g.itemModelOutput.accept(ModBlocks.IRON_LATTICE.get().asItem(), ItemModelUtils.tintedModel(existing("lattice_iron_inventory"),
				ItemModelUtils.constantTint(-1), ItemModelUtils.constantTint(FoliageColor.FOLIAGE_DEFAULT)));
	}

	private static void furnitureModels(BlockModelGenerators g) {
		for (WoodVariant wood : WoodVariant.values()) {
			String name = wood.getSerializedName();
			TextureMapping planks = TextureMapping.singleSlot(PLANKS, new Material(wood.planksTexture()));

			Block chair = ModBlocks.CHAIRS.get(wood).get();
			Identifier chairModel = CHAIR.create(RusticModelProvider.existingBlockModel("chair_" + name), planks, g.modelOutput);
			g.blockStateOutput.accept(MultiVariantGenerator.dispatch(chair, BlockModelGenerators.plainVariant(chairModel))
					.with(PropertyDispatch.modify(HorizontalDirectionalBlock.FACING).generate(facing -> yRot(wallRotation(facing, 0)))));
			g.registerSimpleItemModel(chair, chairModel);

			Block table = ModBlocks.TABLES.get(wood).get();
			MultiVariant top = BlockModelGenerators.plainVariant(TABLE_TOP.create(RusticModelProvider.existingBlockModel("table_" + name + "_top"), planks,
					g.modelOutput));
			MultiVariant leg = BlockModelGenerators.plainVariant(TABLE_LEG.create(RusticModelProvider.existingBlockModel("table_" + name + "_leg"), planks,
					g.modelOutput));
			Identifier tableModel = TABLE.create(RusticModelProvider.existingBlockModel("table_" + name), planks, g.modelOutput);
			MultiPartGenerator multipart = MultiPartGenerator.multiPart(table).with(top);
			BooleanProperty[] corners = { TableBlock.NW, TableBlock.NE, TableBlock.SE, TableBlock.SW };
			for (int i = 0; i < corners.length; i++) {
				multipart.with(BlockModelGenerators.condition(corners[i], true), leg.with(yRot(90 * i)));
			}
			g.blockStateOutput.accept(multipart);
			g.registerSimpleItemModel(table, tableModel);
		}
	}

	/** Template model {@code rusticrevived:block/<parent>} textured by its {@code #planks} slot. */
	private static ModelTemplate template(String parent) {
		return new ModelTemplate(Optional.of(RusticModelProvider.existingBlockModel(parent)), Optional.empty(), PLANKS);
	}

	private static Identifier existing(String model) {
		return RusticModelProvider.existingBlockModel(model);
	}

	private static Material tex(String path) {
		return new Material(RusticModelProvider.blockTex(path));
	}

	private static Material mcTex(String path) {
		return new Material(Identifier.withDefaultNamespace("block/" + path));
	}

	private static VariantMutator yRot(int degrees) {
		return VariantMutator.Y_ROT.withValue(Quadrant.parseJson(degrees));
	}

	private static VariantMutator xRot(int degrees) {
		return VariantMutator.X_ROT.withValue(Quadrant.parseJson(degrees));
	}

	/** Cube with the same texture on every side; the block item uses the block model. */
	private static void cube(BlockModelGenerators g, Block block, String texture) {
		Identifier model = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(tex(texture)), g.modelOutput);
		g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
		g.registerSimpleItemModel(block, model);
	}

	private static void pillar(BlockModelGenerators g, Block block, String side, Material end) {
		TextureMapping textures = TextureMapping.column(tex(side), end);
		Identifier vertical = ModelTemplates.CUBE_COLUMN.create(block, textures, g.modelOutput);
		Identifier horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(block, textures, g.modelOutput);
		g.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block, BlockModelGenerators.plainVariant(vertical),
				BlockModelGenerators.plainVariant(horizontal)));
		g.registerSimpleItemModel(block, vertical);
	}

	private static void stairs(BlockModelGenerators g, Block block, String texture) {
		TextureMapping textures = TextureMapping.cube(tex(texture));
		Identifier straight = ModelTemplates.STAIRS_STRAIGHT.create(block, textures, g.modelOutput);
		Identifier inner = ModelTemplates.STAIRS_INNER.create(block, textures, g.modelOutput);
		Identifier outer = ModelTemplates.STAIRS_OUTER.create(block, textures, g.modelOutput);
		g.blockStateOutput.accept(BlockModelGenerators.createStairs(block, BlockModelGenerators.plainVariant(inner), BlockModelGenerators.plainVariant(straight),
				BlockModelGenerators.plainVariant(outer)));
		g.registerSimpleItemModel(block, straight);
	}

	/** Slab of the cube block {@code full} (whose model is used for double slabs). */
	private static void slab(BlockModelGenerators g, Block block, String full) {
		TextureMapping textures = TextureMapping.cube(tex(full));
		Identifier bottom = ModelTemplates.SLAB_BOTTOM.create(block, textures, g.modelOutput);
		Identifier top = ModelTemplates.SLAB_TOP.create(block, textures, g.modelOutput);
		g.blockStateOutput.accept(BlockModelGenerators.createSlab(block, BlockModelGenerators.plainVariant(bottom), BlockModelGenerators.plainVariant(top),
				BlockModelGenerators.plainVariant(RusticModelProvider.existingBlockModel(full))));
		g.registerSimpleItemModel(block, bottom);
	}

	/** Rope or chain: vertical or horizontal line plus the dangling parts of horizontal lines (legacy multipart). */
	private static void line(BlockModelGenerators g, Block block, Identifier vertical, Identifier horizontal, List<Identifier> dangle) {
		MultiVariant horizontalModel = BlockModelGenerators.plainVariant(horizontal);
		MultiPartGenerator multipart = MultiPartGenerator.multiPart(block)
				.with(BlockModelGenerators.condition(RopeBaseBlock.AXIS, Direction.Axis.Y), BlockModelGenerators.plainVariant(vertical))
				.with(BlockModelGenerators.condition(RopeBaseBlock.AXIS, Direction.Axis.Z), horizontalModel)
				.with(BlockModelGenerators.condition(RopeBaseBlock.AXIS, Direction.Axis.X), horizontalModel.with(yRot(90)));
		for (Identifier model : dangle) {
			MultiVariant dangleModel = BlockModelGenerators.plainVariant(model);
			multipart.with(BlockModelGenerators.condition(RopeBaseBlock.AXIS, Direction.Axis.Z).term(RopeBaseBlock.DANGLE, true), dangleModel)
					.with(BlockModelGenerators.condition(RopeBaseBlock.AXIS, Direction.Axis.X).term(RopeBaseBlock.DANGLE, true), dangleModel.with(yRot(90)));
		}
		g.blockStateOutput.accept(multipart);
	}

	/** Candle, iron torch or wood lantern; {@code wallOffset} is the model rotation of {@code facing=north}. */
	private static void mountedLight(BlockModelGenerators g, Block block, Identifier standing, Identifier wall, int wallOffset) {
		MultiVariant standingModel = BlockModelGenerators.plainVariant(standing);
		MultiVariant wallModel = BlockModelGenerators.plainVariant(wall);
		g.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(MountedLightBlock.FACING)
				.generate(facing -> facing == Direction.UP ? standingModel : wallModel.with(yRot(wallRotation(facing, wallOffset))))));
	}

	private static void candleLever(BlockModelGenerators g, Block block, String metal) {
		MultiVariant standing = BlockModelGenerators.plainVariant(existing("candle" + metal));
		MultiVariant wall = BlockModelGenerators.plainVariant(existing("candle_wall" + metal));
		MultiVariant standingPulled = BlockModelGenerators.plainVariant(existing("candle_pulled" + metal));
		MultiVariant wallPulled = BlockModelGenerators.plainVariant(existing("candle_wall_pulled" + metal));
		g.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(MountedLightBlock.FACING, CandleLeverBlock.POWERED)
				.generate((facing, pulled) -> facing == Direction.UP ? pulled ? standingPulled : standing
						: (pulled ? wallPulled : wall).with(yRot(wallRotation(facing, 0))))));
	}

	private static void doubleCandle(BlockModelGenerators g, Block block, Identifier standing, Identifier wall) {
		MultiVariant standingModel = BlockModelGenerators.plainVariant(standing);
		MultiVariant wallModel = BlockModelGenerators.plainVariant(wall);
		g.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(DoubleCandleBlock.FACING)
				.generate(mount -> switch (mount) {
					case UP_X -> standingModel.with(yRot(90));
					case UP_Z -> standingModel;
					default -> wallModel.with(yRot(wallRotation(mount.facing(), 0)));
				})));
	}

	private static void lantern(BlockModelGenerators g, Block block, String metal) {
		MultiVariant up = BlockModelGenerators.plainVariant(existing("lantern_up" + metal));
		MultiVariant down = BlockModelGenerators.plainVariant(existing("lantern_down" + metal));
		MultiVariant wall = BlockModelGenerators.plainVariant(existing("lantern_wall" + metal));
		g.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(PropertyDispatch.initial(RusticLanternBlock.FACING)
				.generate(facing -> switch (facing) {
					case UP -> up;
					case DOWN -> down;
					default -> wall.with(yRot(wallRotation(facing, 0)));
				})));
	}

	/** Y rotation of a wall model whose unrotated model is the {@code facing=north} one rotated by {@code northRotation}. */
	private static int wallRotation(Direction facing, int northRotation) {
		return ((int) facing.toYRot() + 180 + northRotation) % 360;
	}

	/** Core, bars towards every connection, and the same with leaves when the lattice is leafy (legacy LatticeModel). */
	private static void lattice(BlockModelGenerators g) {
		MultiVariant bar = BlockModelGenerators.plainVariant(existing("lattice_iron_bar"));
		MultiVariant leavesBar = BlockModelGenerators.plainVariant(existing("lattice_leaves_bar"));
		MultiPartGenerator multipart = MultiPartGenerator.multiPart(ModBlocks.IRON_LATTICE.get())
				.with(BlockModelGenerators.plainVariant(existing("lattice_iron_base")))
				.with(BlockModelGenerators.condition(LatticeBlock.LEAVES, true), BlockModelGenerators.plainVariant(existing("lattice_leaves_base")));
		for (Direction direction : Direction.values()) {
			int x = direction == Direction.DOWN ? 90 : direction == Direction.UP ? 270 : 0;
			int y = direction.getAxis().isHorizontal() ? wallRotation(direction, 0) : 0;
			VariantMutator rotation = xRot(x).then(yRot(y));
			BooleanProperty connection = LatticeBlock.CONNECTIONS.get(direction);
			multipart.with(BlockModelGenerators.condition(connection, true), bar.with(rotation))
					.with(BlockModelGenerators.condition(connection, true).term(LatticeBlock.LEAVES, true), leavesBar.with(rotation));
		}
		g.blockStateOutput.accept(multipart);
	}

	// ================================================================ recipes

	public static void recipes(RusticRecipeProvider p) {
		stoneRecipes(p);
		woodRecipes(p);
		metalRecipes(p);
	}

	private static void stoneRecipes(RusticRecipeProvider p) {
		RecipeOutput out = p.output();
		RecipeOutput pillars = p.whenConfig("enablePillars");
		pillar(p, pillars, "pillar_stone", ModBlocks.STONE_PILLAR.get(), Ingredient.of(Blocks.STONE), p.hasItem(Blocks.STONE));
		pillar(p, pillars, "pillar_andesite", ModBlocks.ANDESITE_PILLAR.get(), Ingredient.of(Blocks.ANDESITE), p.hasItem(Blocks.ANDESITE));
		pillar(p, pillars, "pillar_diorite", ModBlocks.DIORITE_PILLAR.get(), Ingredient.of(Blocks.DIORITE), p.hasItem(Blocks.DIORITE));
		pillar(p, pillars, "pillar_granite", ModBlocks.GRANITE_PILLAR.get(), Ingredient.of(Blocks.GRANITE), p.hasItem(Blocks.GRANITE));
		pillar(p, pillars, "pillar_basalt", ModBlocks.BASALT_PILLAR.get(), Ingredient.of(Blocks.POLISHED_BASALT), p.hasItem(Blocks.POLISHED_BASALT));
		pillar(p, withTag(p, "enablePillars", ModTags.Items.LIMESTONE_STONES), "pillar_limestone", ModBlocks.LIMESTONE_PILLAR.get(),
				p.ingredient(ModTags.Items.LIMESTONE_STONES), p.hasTag(ModTags.Items.LIMESTONE_STONES));
		pillar(p, withTag(p, "enablePillars", ModTags.Items.MARBLE_STONES), "pillar_marble", ModBlocks.MARBLE_PILLAR.get(),
				p.ingredient(ModTags.Items.MARBLE_STONES), p.hasTag(ModTags.Items.MARBLE_STONES));
		RecipeOutput slatePillars = p.withConditions(ConfigCondition.of("enablePillars"), ConfigCondition.of("enableSlate"));
		pillar(p, slatePillars, "pillar_slate", ModBlocks.SLATE_PILLAR.get(), Ingredient.of(ModBlocks.SLATE), p.hasItem(ModBlocks.SLATE));
		stonecutting(p, slatePillars, ModBlocks.SLATE.get(), ModBlocks.SLATE_PILLAR.get(), 1);

		RecipeOutput slate = p.whenConfig("enableSlate");
		ItemLike slateItem = ModBlocks.SLATE.get();
		shaped(p, slate, "slate_roof", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_ROOF.get(), 4, slateItem, "SS", "SS");
		shaped(p, slate, "slate_roof_stairs", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_ROOF_STAIRS.get(), 4, ModBlocks.SLATE_ROOF.get(),
				"S  ", "SS ", "SSS");
		shaped(p, slate, "slate_roof_slab", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_ROOF_SLAB.get(), 6, ModBlocks.SLATE_ROOF.get(), "SSS");
		shaped(p, slate, "slate_brick_stairs", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_BRICK_STAIRS.get(), 4, ModBlocks.SLATE_BRICK.get(),
				"S  ", "SS ", "SSS");
		shaped(p, slate, "slate_brick_slab", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_BRICK_SLAB.get(), 6, ModBlocks.SLATE_BRICK.get(), "SSS");
		shaped(p, slate, "slate_tile", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_TILE.get(), 1, slateItem, "S");
		shaped(p, slate, "slate_tile_from_slate_brick", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_TILE.get(), 1, ModBlocks.SLATE_BRICK.get(), "S");
		shaped(p, slate, "slate_brick", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_BRICK.get(), 4, ModBlocks.SLATE_TILE.get(), "SS", "SS");
		shaped(p, slate, "slate_chiseled", RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_CHISELED.get(), 4, ModBlocks.SLATE_BRICK.get(), "SS", "SS");
		p.shapedRecipe(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SLATE_PAVEMENT.get(), 4)
				.pattern("SB").pattern("BS").define('S', slateItem).define('B', ModBlocks.SLATE_BRICK.get())
				.unlockedBy(RusticRecipeProvider.hasName(slateItem), p.hasItem(slateItem))
				.save(slate, RusticRecipeProvider.key("slate_pavement"));

		for (Block result : List.of(ModBlocks.SLATE_ROOF.get(), ModBlocks.SLATE_TILE.get(), ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_CHISELED.get(),
				ModBlocks.SLATE_PAVEMENT.get(), ModBlocks.SLATE_ROOF_STAIRS.get(), ModBlocks.SLATE_BRICK_STAIRS.get())) {
			stonecutting(p, slate, ModBlocks.SLATE.get(), result, 1);
		}
		stonecutting(p, slate, ModBlocks.SLATE.get(), ModBlocks.SLATE_ROOF_SLAB.get(), 2);
		stonecutting(p, slate, ModBlocks.SLATE.get(), ModBlocks.SLATE_BRICK_SLAB.get(), 2);
		stonecutting(p, slate, ModBlocks.SLATE_ROOF.get(), ModBlocks.SLATE_ROOF_STAIRS.get(), 1);
		stonecutting(p, slate, ModBlocks.SLATE_ROOF.get(), ModBlocks.SLATE_ROOF_SLAB.get(), 2);
		stonecutting(p, slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_BRICK_STAIRS.get(), 1);
		stonecutting(p, slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_BRICK_SLAB.get(), 2);
		stonecutting(p, slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_CHISELED.get(), 1);
		stonecutting(p, slate, ModBlocks.SLATE_BRICK.get(), ModBlocks.SLATE_TILE.get(), 1);
		stonecutting(p, slate, ModBlocks.SLATE_TILE.get(), ModBlocks.SLATE_BRICK.get(), 1);

		RecipeOutput clayWalls = p.whenConfig("enableClayWalls");
		clayWall(p, clayWalls, ModBlocks.CLAY_WALL.get(), 8, " P ", "PCP", " P ");
		clayWall(p, clayWalls, ModBlocks.CLAY_WALL_CROSS.get(), 1, "P P", " C ", "P P");
		clayWall(p, clayWalls, ModBlocks.CLAY_WALL_DIAG.get(), 1, "P  ", " C ", "  P");

		p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.GARGOYLE.get(), 2)
				.pattern("PRP").pattern("SSS")
				.define('P', Blocks.STONE_PRESSURE_PLATE).define('R', Blocks.STONE).define('S', Blocks.SMOOTH_STONE_SLAB)
				.unlockedBy(RusticRecipeProvider.hasName(Blocks.STONE), p.hasItem(Blocks.STONE))
				.save(out, RusticRecipeProvider.key("gargoyle"));
	}

	/** Clay wall recipe: 'P' is any planks, 'C' clay for the plain wall and a clay wall for the beamed ones. */
	private static void clayWall(RusticRecipeProvider p, RecipeOutput out, Block result, int count, String... pattern) {
		ItemLike clay = result == ModBlocks.CLAY_WALL.get() ? Blocks.CLAY : ModBlocks.CLAY_WALL.get();
		ShapedRecipeBuilder builder = p.shapedRecipe(RecipeCategory.BUILDING_BLOCKS, result, count).define('P', ItemTags.PLANKS).define('C', clay);
		for (String line : pattern) {
			builder.pattern(line);
		}
		builder.unlockedBy(RusticRecipeProvider.hasName(clay), p.hasItem(clay)).save(out, RusticRecipeProvider.key(itemName(result)));
	}

	/** Recipe made of a single ingredient 'S'. */
	private static void shaped(RusticRecipeProvider p, RecipeOutput out, String id, RecipeCategory category, ItemLike result, int count, ItemLike input,
			String... pattern) {
		ShapedRecipeBuilder builder = p.shapedRecipe(category, result, count).define('S', input);
		for (String line : pattern) {
			builder.pattern(line);
		}
		builder.unlockedBy(RusticRecipeProvider.hasName(input), p.hasItem(input)).save(out, RusticRecipeProvider.key(id));
	}

	private static void pillar(RusticRecipeProvider p, RecipeOutput out, String id, Block result, Ingredient stone, Criterion<?> unlock) {
		p.shapedRecipe(RecipeCategory.BUILDING_BLOCKS, result, 6).pattern("SS").pattern("SS").pattern("SS").define('S', stone)
				.unlockedBy("has_stone", unlock).save(out, RusticRecipeProvider.key(id));
	}

	private static void stonecutting(RusticRecipeProvider p, RecipeOutput out, ItemLike input, ItemLike result, int count) {
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(input), RecipeCategory.BUILDING_BLOCKS, result, count)
				.unlockedBy(RusticRecipeProvider.hasName(input), p.hasItem(input))
				.save(out, RusticRecipeProvider.key(itemName(result) + "_from_" + itemName(input) + "_stonecutting"));
	}

	private static String itemName(ItemLike item) {
		return RusticRecipeProvider.itemName(item);
	}

	/** Output enabled by a config option and only loaded when some item has the (modded) tag. */
	private static RecipeOutput withTag(RusticRecipeProvider p, String option, TagKey<Item> tag) {
		return p.withConditions(ConfigCondition.of(option), new NotCondition(new TagEmptyCondition<>(tag)));
	}

	private static void woodRecipes(RusticRecipeProvider p) {
		RecipeOutput chairs = p.whenConfig("enableChairs");
		RecipeOutput tables = p.whenConfig("enableTables");
		for (WoodVariant wood : WoodVariant.values()) {
			Block planks = wood.planks();
			p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.CHAIRS.get(wood).get(), 4)
					.pattern("P  ").pattern("PPP").pattern("S S").define('P', planks).define('S', Tags.Items.RODS_WOODEN).group(RusticRevived.NAMESPACE + ":chair")
					.unlockedBy(RusticRecipeProvider.hasName(planks), p.hasItem(planks))
					.save(chairs, RusticRecipeProvider.key(wood.getSerializedName() + "_chair"));
			p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.TABLES.get(wood).get(), 2)
					.pattern("PPP").pattern("S S").define('P', planks).define('S', Tags.Items.RODS_WOODEN).group(RusticRevived.NAMESPACE + ":table")
					.unlockedBy(RusticRecipeProvider.hasName(planks), p.hasItem(planks))
					.save(tables, RusticRecipeProvider.key(wood.getSerializedName() + "_table"));
		}

		RecipeOutput painted = p.whenConfig("enablePaintedWood");
		for (DyeColor color : DyeColor.values()) {
			p.shapedRecipe(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PAINTED_WOOD.get(color).get(), 8)
					.pattern("PPP").pattern("PDP").pattern("PPP").define('P', ItemTags.PLANKS).define('D', color.getTag())
					.group(RusticRevived.NAMESPACE + ":painted_wood")
					.unlockedBy("has_dye", p.hasTag(color.getTag()))
					.save(painted, RusticRecipeProvider.key("painted_wood_" + color.getSerializedName()));
		}

		p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.LANTERN_WOOD.get(), 1)
				.pattern("SPS").pattern("GTG").pattern("SPS")
				.define('S', Tags.Items.RODS_WOODEN).define('P', ItemTags.PLANKS).define('G', Tags.Items.GLASS_PANES_COLORLESS).define('T', Items.TORCH)
				.unlockedBy(RusticRecipeProvider.hasName(Items.TORCH), p.hasItem(Items.TORCH))
				.save(p.output(), RusticRecipeProvider.key("lantern_wood"));
	}

	private static void metalRecipes(RusticRecipeProvider p) {
		RecipeOutput out = p.output();
		metalSet(p, out, "", Tags.Items.INGOTS_IRON, ModBlocks.CANDLE.get(), ModBlocks.CANDLE_DOUBLE.get(), ModBlocks.CANDLE_LEVER.get(),
				ModBlocks.CHAIN.get(), ModBlocks.CHANDELIER.get(), ModBlocks.IRON_LANTERN.get(), "iron_lantern");
		metalSet(p, out, "_gold", Tags.Items.INGOTS_GOLD, ModBlocks.CANDLE_GOLD.get(), ModBlocks.CANDLE_DOUBLE_GOLD.get(), ModBlocks.CANDLE_LEVER_GOLD.get(),
				ModBlocks.CHAIN_GOLD.get(), ModBlocks.CHANDELIER_GOLD.get(), ModBlocks.GOLDEN_LANTERN.get(), "golden_lantern");
		ICondition silverExists = new NotCondition(new TagEmptyCondition<>(ModTags.Items.INGOTS_SILVER));
		metalSet(p, p.withConditions(ConfigCondition.of("enableSilverDecor"), silverExists), "_silver", ModTags.Items.INGOTS_SILVER,
				ModBlocks.CANDLE_SILVER.get(), ModBlocks.CANDLE_DOUBLE_SILVER.get(), ModBlocks.CANDLE_LEVER_SILVER.get(), ModBlocks.CHAIN_SILVER.get(),
				ModBlocks.CHANDELIER_SILVER.get(), ModBlocks.SILVER_LANTERN.get(), "silver_lantern");

		p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.IRON_TORCH.get(), 4)
				.pattern("I").pattern("C").pattern("S")
				.define('I', Tags.Items.NUGGETS_IRON).define('C', ItemTags.COALS).define('S', Tags.Items.RODS_WOODEN)
				.unlockedBy("has_iron_nugget", p.hasTag(Tags.Items.NUGGETS_IRON))
				.save(out, RusticRecipeProvider.key("iron_torch"));
		p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.IRON_LATTICE.get(), 16)
				.pattern(" I ").pattern("III").pattern(" I ").define('I', Tags.Items.INGOTS_IRON)
				.unlockedBy("has_iron_ingot", p.hasTag(Tags.Items.INGOTS_IRON))
				.save(p.whenConfig("enableLattice"), RusticRecipeProvider.key("iron_lattice"));
		p.shapedRecipe(RecipeCategory.DECORATIONS, ModBlocks.ROPE.get(), 12)
				.pattern("S").pattern("S").pattern("S").define('S', Items.STRING)
				.unlockedBy(RusticRecipeProvider.hasName(Items.STRING), p.hasItem(Items.STRING))
				.save(out, RusticRecipeProvider.key("rope"));
	}

	/** Candles, candle lever, chain, chandelier and lantern of one metal; recipe ids end with {@code suffix}. */
	private static void metalSet(RusticRecipeProvider p, RecipeOutput out, String suffix, TagKey<Item> ingot, Block candle, Block doubleCandle, Block lever,
			Block chain, Block chandelier, Block lantern, String lanternId) {
		String hasIngot = "has_ingot";
		p.shapedRecipe(RecipeCategory.DECORATIONS, candle, 4)
				.pattern("S").pattern("W").pattern("I").define('S', Items.STRING).define('W', ModTags.Items.WAX).define('I', ingot)
				.unlockedBy(hasIngot, p.hasTag(ingot)).save(out, RusticRecipeProvider.key("candle" + suffix));
		p.shapedRecipe(RecipeCategory.DECORATIONS, doubleCandle, 4)
				.pattern("S S").pattern("W W").pattern(" I ").define('S', Items.STRING).define('W', ModTags.Items.WAX).define('I', ingot)
				.unlockedBy(hasIngot, p.hasTag(ingot)).save(out, RusticRecipeProvider.key("candle_double" + suffix));
		p.shapedRecipe(RecipeCategory.REDSTONE, lever, 1)
				.pattern("C").pattern("R").define('C', candle).define('R', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy(RusticRecipeProvider.hasName(candle), p.hasItem(candle)).save(out, RusticRecipeProvider.key("candle_lever" + suffix));
		p.shapedRecipe(RecipeCategory.DECORATIONS, chain, 12)
				.pattern("I").pattern("I").pattern("I").define('I', ingot)
				.unlockedBy(hasIngot, p.hasTag(ingot)).save(out, RusticRecipeProvider.key("chain" + suffix));
		p.shapedRecipe(RecipeCategory.DECORATIONS, chandelier, 2)
				.pattern(" I ").pattern("C C").pattern("III").define('I', ingot).define('C', chain)
				.unlockedBy(RusticRecipeProvider.hasName(chain), p.hasItem(chain)).save(out, RusticRecipeProvider.key("chandelier" + suffix));
		p.shapedRecipe(RecipeCategory.DECORATIONS, lantern, 4)
				.pattern("I").pattern("C").pattern("I").define('I', ingot).define('C', ItemTags.COALS)
				.unlockedBy(hasIngot, p.hasTag(ingot)).save(out, RusticRecipeProvider.key(lanternId));
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
		Block[] chains = chains().toArray(Block[]::new);

		t.tagOf(BlockTags.MINEABLE_WITH_PICKAXE).add(pillars).add(slate).add(slateShapes).add(ModBlocks.GARGOYLE.get()).add(metalDecor().toArray(Block[]::new))
				.add(ModBlocks.IRON_TORCH.get());
		t.tagOf(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.CLAY_WALL.get(), ModBlocks.CLAY_WALL_CROSS.get(), ModBlocks.CLAY_WALL_DIAG.get());
		t.tagOf(BlockTags.MINEABLE_WITH_AXE).add(paintedWood).add(chairs).add(tables).add(ModBlocks.LANTERN_WOOD.get());

		t.tagOf(ModTags.Blocks.PILLARS).add(pillars);
		t.tagOf(ModTags.Blocks.PAINTED_WOOD).add(paintedWood);
		t.tagOf(ModTags.Blocks.CHAIRS).add(chairs);
		t.tagOf(ModTags.Blocks.TABLES).add(tables);
		t.tagOf(ModTags.Blocks.SLATE_STONES).add(ModBlocks.SLATE.get());
		t.tagOf(ModTags.Blocks.CHANDELIER_SUPPORTS).add(Blocks.IRON_CHAIN, ModBlocks.IRON_LATTICE.get());
		t.tagOf(BlockTags.PLANKS).add(paintedWood);
		t.tagOf(BlockTags.STAIRS).add(ModBlocks.SLATE_ROOF_STAIRS.get(), ModBlocks.SLATE_BRICK_STAIRS.get());
		t.tagOf(BlockTags.SLABS).add(ModBlocks.SLATE_ROOF_SLAB.get(), ModBlocks.SLATE_BRICK_SLAB.get());
		t.tagOf(BlockTags.CLIMBABLE).add(ModBlocks.ROPE.get()).add(chains);
		t.tagOf(Tags.Blocks.CHAINS).add(chains);
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
