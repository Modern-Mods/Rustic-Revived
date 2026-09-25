package nadiendev.rusticrevived.datagen.providers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * Dynamic Trees treepack (trees/rusticrevived/...): olive and ironwood families, species and
 * leaves properties, the biome populator, plus the assets (block states, models) and loot tables
 * of the blocks Dynamic Trees registers for them. Only read when Dynamic Trees is installed; the
 * JoCodes (tree shapes for world generation) are static resources in
 * {@code trees/rusticrevived/jo_codes}.
 * <p>
 * Registered by Dynamic Trees under the {@value RusticRevived#NAMESPACE} namespace:
 * {@code <tree>_branch} (block + item), {@code <tree>_dynamic_leaves}, {@code <tree>_dynamic_sapling}
 * and {@code <tree>_seed}. Leaves and sapling get custom names because {@code <tree>_leaves} and
 * {@code <tree>_sapling} already are Rustic's own blocks.
 */
public class RusticTreepackProvider implements DataProvider {
	private static final String NS = RusticRevived.NAMESPACE;

	/**
	 * A Rustic dynamic tree (legacy TreeOlive / TreeIronwood).
	 *
	 * @param fruit          fruit dropped by the leaves
	 * @param fruitChance    1 in fruitChance leaves drop a fruit (legacy DropCreatorFruit)
	 * @param biomeWeight    weight among 1000 of the tree in its biomes (legacy BiomeDataBasePopulator: gen chance * 500)
	 */
	private record Tree(String name, float tapering, float signalEnergy, int upProbability, int lowestBranchHeight, String fruit, int fruitChance,
			String biomeTag, int biomeWeight) {
	}

	private static final List<Tree> TREES = List.of(
			new Tree("olive", 0.3F, 12.0F, 2, 3, "olives", 18, ModTags.Biomes.HAS_OLIVE_TREES.location().toString(), 15),
			new Tree("ironwood", 0.4F, 14.0F, 4, 4, "ironberries", 48, ModTags.Biomes.HAS_IRONWOOD_TREES.location().toString(), 7));

	private static final String SHEARS_OR_SILK_TOUCH = """
			{"condition": "minecraft:any_of", "terms": [
				{"condition": "minecraft:match_tool", "predicate": {"items": "minecraft:shears"}},
				{"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
					{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]}""";

	private final PackOutput output;

	public RusticTreepackProvider(PackOutput output) {
		this.output = output;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		List<CompletableFuture<?>> writes = new ArrayList<>();
		StringBuilder populators = new StringBuilder();
		for (Tree tree : TREES) {
			treepack(cache, writes, tree);
			assets(cache, writes, tree);
			lootTables(cache, writes, tree);
			populators.append(populators.isEmpty() ? "" : ",").append("""
					{
						"_comment": "legacy BiomeDataBasePopulator: %1$s trees are spliced before the biome's own trees",
						"select": {"name": ".*", "tag": "#%2$s"},
						"apply": {"species": {"method": "splice_before", "random": {"%3$s:%1$s": %4$d, "...": %5$d}}}
					}""".formatted(tree.name(), tree.biomeTag(), NS, tree.biomeWeight(), 1000 - tree.biomeWeight()));
		}
		writes.add(save(cache, "trees/" + NS + "/world_gen/default.json", "[" + populators + "]"));
		writes.add(save(cache, "resourcepacks/dynamictrees/pack.mcmeta", """
				{"pack": {"description": "Rustic Revived: Dynamic Trees assets", "min_format": 84, "max_format": 84}}"""));
		return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
	}

	private void treepack(CachedOutput cache, List<CompletableFuture<?>> writes, Tree tree) {
		String name = tree.name();
		String trees = "trees/" + NS + "/";
		writes.add(save(cache, trees + "leaves_properties/" + name + ".json", """
				{"primitive_leaves": "%1$s:%2$s_leaves", "block_registry_name": "%1$s:%2$s_dynamic_leaves"}""".formatted(NS, name)));
		writes.add(save(cache, trees + "families/" + name + ".json", """
				{
					"common_leaves": "%1$s:%2$s",
					"common_species": "%1$s:%2$s",
					"primitive_log": "%1$s:%2$s_log",
					"generate_stripped_branch": false,
					"max_branch_radius": 8
				}""".formatted(NS, name)));
		writes.add(save(cache, trees + "species/" + name + ".json", """
				{
					"family": "%1$s:%2$s",
					"leaves_properties": "%1$s:%2$s",
					"tapering": %3$s,
					"signal_energy": %4$s,
					"up_probability": %5$d,
					"lowest_branch_height": %6$d,
					"growth_rate": 1.0,
					"preferred_climate": "temperate",
					"climate_tolerance": 0.5,
					"primitive_sapling": "%1$s:%2$s_sapling",
					"sapling_name": "%2$s_dynamic_sapling"
				}""".formatted(NS, name, tree.tapering(), tree.signalEnergy(), tree.upProbability(), tree.lowestBranchHeight())));
	}

	/**
	 * Client assets in the 26.1 / Dynamic Trees 1.8 format: branches use Dynamic Trees' custom block
	 * state model type ({@code dynamictrees:branch}), items are declared by {@code items/} client
	 * item definitions.
	 */
	private void assets(CachedOutput cache, List<CompletableFuture<?>> writes, Tree tree) {
		String name = tree.name();
		String assets = "resourcepacks/dynamictrees/assets/" + NS + "/";
		String bark = """
				"textures": {"bark": "%1$s:block/log_%2$s", "rings": "%1$s:block/log_%2$s_top"}""".formatted(NS, name);
		writes.add(save(cache, assets + "blockstates/" + name + "_branch.json", """
				{"variants": {"": {"type": "dynamictrees:branch", "family": "%s:%s", %s}}}""".formatted(NS, name, bark)));
		writes.add(save(cache, assets + "models/item/" + name + "_branch.json", "{\"parent\": \"dynamictrees:item/branch\", " + bark + "}"));
		writes.add(save(cache, assets + "items/" + name + "_branch.json", itemDefinition(NS + ":item/" + name + "_branch")));
		writes.add(save(cache, assets + "blockstates/" + name + "_dynamic_leaves.json", variant(NS + ":block/" + name + "_leaves")));
		writes.add(save(cache, assets + "blockstates/" + name + "_dynamic_sapling.json", variant(NS + ":block/saplings/" + name)));
		writes.add(save(cache, assets + "models/block/saplings/" + name + ".json", """
				{
					"parent": "dynamictrees:block/smartmodel/sapling",
					"textures": {"leaves": "%1$s:block/leaves_%2$s", "log": "%1$s:block/log_%2$s"}
				}""".formatted(NS, name)));
		writes.add(save(cache, assets + "models/item/" + name + "_seed.json", """
				{"parent": "dynamictrees:item/standard_seed", "textures": {"layer0": "%1$s:item/%2$sseed"}}""".formatted(NS, name)));
		writes.add(save(cache, assets + "items/" + name + "_seed.json", itemDefinition(NS + ":item/" + name + "_seed")));
	}

	private static String variant(String model) {
		return "{\"variants\": {\"\": {\"model\": \"" + model + "\"}}}";
	}

	private static String itemDefinition(String model) {
		return "{\"model\": {\"type\": \"minecraft:model\", \"model\": \"" + model + "\"}}";
	}

	/** Loot tables Dynamic Trees looks up for the branches, the leaves and the voluntary seed drops. */
	private void lootTables(CachedOutput cache, List<CompletableFuture<?>> writes, Tree tree) {
		String name = tree.name();
		String loot = "data/" + NS + "/loot_table/";
		writes.add(save(cache, loot + "trees/branches/" + name + "_branch.json", """
				{"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "dynamictrees"}], "type": "minecraft:block", "random_sequence": "%1$s:trees/branches/%2$s_branch", "pools": [
					{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "%1$s:%2$s_log", "functions": [
						{"function": "dynamictrees:multiply_logs_count"}, {"function": "minecraft:explosion_decay"}]}]},
					{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:stick", "functions": [
						{"function": "dynamictrees:multiply_sticks_count"}, {"function": "minecraft:explosion_decay"}]}]}]}"""
				.formatted(NS, name)));
		writes.add(save(cache, loot + "trees/voluntary/" + name + ".json", """
				{"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "dynamictrees"}], "type": "minecraft:block", "random_sequence": "%1$s:trees/voluntary/%2$s", "pools": [
					{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "%1$s:%2$s_seed",
						"conditions": [{"condition": "dynamictrees:voluntary_seed_drop_chance", "rarity": 1.0}]}]}]}""".formatted(NS, name)));
		// leaves of a felled tree: seeds, fruits and sticks
		writes.add(save(cache, loot + "trees/leaves/" + name + ".json", """
				{"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "dynamictrees"}], "type": "minecraft:block", "random_sequence": "%s:trees/leaves/%s", "pools": [%s, %s, %s]}"""
				.formatted(NS, name, seedPool(null), fruitPool(tree, false), stickPool(false))));
		// leaves broken by hand: themselves with shears or silk touch
		writes.add(save(cache, loot + "blocks/" + name + "_dynamic_leaves.json", """
				{"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "dynamictrees"}], "type": "minecraft:block", "random_sequence": "%s:blocks/%s_dynamic_leaves", "pools": [%s, %s, %s]}"""
				.formatted(NS, name, seedPool(NS + ":" + name + "_leaves"), fruitPool(tree, true), stickPool(true))));
	}

	/** The species' seed (or {@code shearedLeaves} when sheared). */
	private static String seedPool(@Nullable String shearedLeaves) {
		String seed = """
				{"type": "dynamictrees:seed_item", "conditions": [
					{"condition": "minecraft:survives_explosion"},
					{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.015625, 0.03125, 0.046875, 0.0625]},
					{"condition": "dynamictrees:seasonal_seed_drop_chance"}]}""";
		String entry = shearedLeaves == null ? seed : """
				{"type": "minecraft:alternatives", "children": [
					{"type": "minecraft:item", "name": "%s", "conditions": [%s]}, %s]}""".formatted(shearedLeaves, SHEARS_OR_SILK_TOUCH, seed);
		return "{\"rolls\": 1, \"entries\": [" + entry + "]}";
	}

	/** Legacy DropCreatorFruit: 1 in (chance - fortune) leaves drop a fruit. */
	private static String fruitPool(Tree tree, boolean unlessSheared) {
		float chance = tree.fruitChance();
		return """
				{"rolls": 1, %s"entries": [{"type": "minecraft:item", "name": "%s:%s", "conditions": [
					{"condition": "minecraft:survives_explosion"},
					{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [%s, %s, %s, %s]}]}]}"""
				.formatted(unlessSheared(unlessSheared), NS, tree.fruit(), 1 / chance, 1 / (chance - 1), 1 / (chance - 2), 1 / (chance - 3));
	}

	private static String stickPool(boolean unlessSheared) {
		return """
				{"rolls": 1, %s"entries": [{"type": "minecraft:item", "name": "minecraft:stick",
					"conditions": [{"condition": "minecraft:table_bonus", "enchantment": "minecraft:fortune", "chances": [0.02, 0.022222223, 0.025, 0.033333335, 0.1]}],
					"functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1.0, "max": 2.0}, "add": false},
						{"function": "minecraft:explosion_decay"}]}]}""".formatted(unlessSheared(unlessSheared));
	}

	private static String unlessSheared(boolean unlessSheared) {
		return unlessSheared ? "\"conditions\": [{\"condition\": \"minecraft:inverted\", \"term\": " + SHEARS_OR_SILK_TOUCH + "}], " : "";
	}

	private CompletableFuture<?> save(CachedOutput cache, String path, String json) {
		JsonElement element = JsonParser.parseString(json);
		Path target = output.getOutputFolder().resolve(path);
		return DataProvider.saveStable(cache, element, target);
	}

	public PackOutput output() {
		return output;
	}

	@Override
	public String getName() {
		return "Dynamic Trees treepack: " + RusticRevived.NAMESPACE;
	}
}
