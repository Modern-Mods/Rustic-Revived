package nadiendev.rusticrevived.compat.kubejs;

import java.util.List;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Condenser recipe created from a script: like the legacy CraftTweaker support, a recipe with a
 * modifier or more than two ingredients is an advanced recipe even if {@code advanced} was not set.
 */
public class CondenserKubeRecipe extends KubeRecipe {

	@Override
	public void serialize() {
		List<Ingredient> ingredients = getValue(RusticRecipeSchemas.CONDENSER_INGREDIENTS);
		Ingredient modifier = getValue(RusticRecipeSchemas.CONDENSER_MODIFIER);
		boolean needsAdvanced = (ingredients != null && ingredients.size() > 2) || (modifier != null && !modifier.isEmpty());
		if (needsAdvanced && !Boolean.TRUE.equals(getValue(RusticRecipeSchemas.CONDENSER_ADVANCED))) {
			setValue(RusticRecipeSchemas.CONDENSER_ADVANCED, true);
		}
		super.serialize();
	}
}
