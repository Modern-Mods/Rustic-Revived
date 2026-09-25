package nadiendev.rusticrevived.datagen.providers;

import java.util.concurrent.CompletableFuture;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.datagen.providers.parts.AlchemyData;
import nadiendev.rusticrevived.datagen.providers.parts.DecorData;
import nadiendev.rusticrevived.datagen.providers.parts.FarmData;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import nadiendev.rusticrevived.datagen.providers.parts.StorageData;
import nadiendev.rusticrevived.recipe.ConfigCondition;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Crafting, smelting and machine recipes. Each subsystem generates its own recipes from
 * {@code datagen.providers.parts}; the protected vanilla helpers are re-exposed publicly here.
 */
public class RusticRecipeProvider extends RecipeProvider {

	protected RusticRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		super(registries, output);
	}

	@Override
	protected void buildRecipes() {
		DecorData.recipes(this);
		FarmData.recipes(this);
		StorageData.recipes(this);
		AlchemyData.recipes(this);
		MiscData.recipes(this);
	}

	/** Data provider entry point. */
	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
			return new RusticRecipeProvider(registries, output);
		}

		@Override
		public String getName() {
			return "Recipes: " + RusticRevived.NAMESPACE;
		}
	}

	// ---------------------------------------------------------------- accessors for the parts

	public RecipeOutput output() {
		return output;
	}

	public HolderLookup.Provider registries() {
		return registries;
	}

	public HolderGetter<Item> items() {
		return items;
	}

	/** Output that only loads the recipe when the given boolean config option is enabled. */
	public RecipeOutput whenConfig(String option) {
		return output.withConditions(ConfigCondition.of(option));
	}

	public RecipeOutput withConditions(ICondition... conditions) {
		return output.withConditions(conditions);
	}

	/** Recipe id {@code rusticrevived:<path>}. */
	public static ResourceKey<Recipe<?>> key(String path) {
		return ResourceKey.create(Registries.RECIPE, RusticRevived.id(path));
	}

	public Ingredient ingredient(TagKey<Item> tag) {
		return tag(tag);
	}

	public ShapedRecipeBuilder shapedRecipe(RecipeCategory category, ItemLike result, int count) {
		return shaped(category, result, count);
	}

	public ShapelessRecipeBuilder shapelessRecipe(RecipeCategory category, ItemLike result, int count) {
		return shapeless(category, result, count);
	}

	public Criterion<?> hasItem(ItemLike item) {
		return has(item);
	}

	public Criterion<?> hasTag(TagKey<Item> tag) {
		return has(tag);
	}

	public static String hasName(ItemLike item) {
		return getHasName(item);
	}

	public static String itemName(ItemLike item) {
		return getItemName(item);
	}

	public void stonecutting(RecipeCategory category, ItemLike result, ItemLike base, int count) {
		stonecutterResultFromBase(category, result, base, count);
	}
}
