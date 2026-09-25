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
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Crafting, smelting and machine recipes. Each subsystem generates its own recipes from
 * {@code datagen.providers.parts}.
 */
public class RusticRecipeProvider extends RecipeProvider {
	private HolderLookup.Provider registries;

	public RusticRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput output, HolderLookup.Provider registries) {
		this.registries = registries;
		super.buildRecipes(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput output) {
		DecorData.recipes(this, output);
		FarmData.recipes(this, output);
		StorageData.recipes(this, output);
		AlchemyData.recipes(this, output);
		MiscData.recipes(this, output);
	}

	public HolderLookup.Provider registries() {
		return registries;
	}

	// ---------------------------------------------------------------- helpers shared by the parts

	public static ResourceLocation id(String path) {
		return RusticRevived.id(path);
	}

	/** Output that only loads the recipe when the given boolean config option is enabled. */
	public static RecipeOutput whenConfig(RecipeOutput output, String option) {
		return output.withConditions(ConfigCondition.of(option));
	}

	public static RecipeOutput withConditions(RecipeOutput output, ICondition... conditions) {
		return output.withConditions(conditions);
	}

	public static Criterion<?> hasItem(ItemLike item) {
		return has(item);
	}

	public static Criterion<?> hasTag(TagKey<Item> tag) {
		return has(tag);
	}

	public static String hasName(ItemLike item) {
		return getHasName(item);
	}
}
