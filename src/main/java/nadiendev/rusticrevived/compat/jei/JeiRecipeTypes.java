package nadiendev.rusticrevived.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * JEI recipe categories added by Rustic. Condenser recipes are split in two categories depending
 * on {@link CondenserRecipe#advanced()}.
 */
public final class JeiRecipeTypes {
	public static final RecipeType<RecipeHolder<CrushingTubRecipe>> CRUSHING_TUB = RecipeType.createRecipeHolderType(RusticRevived.id("crushing_tub"));
	public static final RecipeType<RecipeHolder<EvaporatingBasinRecipe>> EVAPORATING = RecipeType.createRecipeHolderType(RusticRevived.id("evaporating"));
	public static final RecipeType<RecipeHolder<CondenserRecipe>> SIMPLE_ALCHEMY = RecipeType.createRecipeHolderType(RusticRevived.id("simple_alchemy"));
	public static final RecipeType<RecipeHolder<CondenserRecipe>> ADVANCED_ALCHEMY = RecipeType.createRecipeHolderType(RusticRevived.id("advanced_alchemy"));
	public static final RecipeType<RecipeHolder<BrewingRecipe>> BREWING = RecipeType.createRecipeHolderType(RusticRevived.id("brewing"));

	private JeiRecipeTypes() {
	}
}
