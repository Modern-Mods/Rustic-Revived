package nadiendev.rusticrevived.compat.jei;

import mezz.jei.api.recipe.types.IRecipeHolderType;
import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;

/**
 * JEI recipe categories added by Rustic. Condenser recipes are split in two categories depending
 * on {@link CondenserRecipe#advanced()}.
 */
public final class JeiRecipeTypes {
	public static final IRecipeHolderType<CrushingTubRecipe> CRUSHING_TUB = IRecipeHolderType.create(RusticRevived.id("crushing_tub"));
	public static final IRecipeHolderType<EvaporatingBasinRecipe> EVAPORATING = IRecipeHolderType.create(RusticRevived.id("evaporating"));
	public static final IRecipeHolderType<CondenserRecipe> SIMPLE_ALCHEMY = IRecipeHolderType.create(RusticRevived.id("simple_alchemy"));
	public static final IRecipeHolderType<CondenserRecipe> ADVANCED_ALCHEMY = IRecipeHolderType.create(RusticRevived.id("advanced_alchemy"));
	public static final IRecipeHolderType<BrewingRecipe> BREWING = IRecipeHolderType.create(RusticRevived.id("brewing"));

	private JeiRecipeTypes() {
	}
}
