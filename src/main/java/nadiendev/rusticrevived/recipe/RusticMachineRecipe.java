package nadiendev.rusticrevived.recipe;

import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Common defaults of Rustic's machine recipes: they never show in the vanilla recipe book, are
 * never unlocked with a toast and can not be auto-placed.
 */
public interface RusticMachineRecipe<T extends RecipeInput> extends Recipe<T> {
	@Override
	default boolean isSpecial() {
		return true;
	}

	@Override
	default boolean showNotification() {
		return false;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return ModRecipes.MACHINES_BOOK_CATEGORY.get();
	}
}
