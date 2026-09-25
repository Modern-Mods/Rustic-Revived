package nadiendev.rusticrevived.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Crushing tub: item -> fluid (+ optional byproduct).
 */
public class CrushingTubCategory extends TexturedRecipeCategory<RecipeHolder<CrushingTubRecipe>> {

	public CrushingTubCategory(IGuiHelper guiHelper) {
		super(guiHelper, JeiRecipeTypes.CRUSHING_TUB, "jei.rusticrevived.recipe.crushing_tub", ModBlocks.CRUSHING_TUB, "jei_crushing_tub", 108, 64);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CrushingTubRecipe> holder, IFocusGroup focuses) {
		CrushingTubRecipe recipe = holder.value();
		builder.addSlot(RecipeIngredientRole.INPUT, 21, 24).addIngredients(recipe.ingredient());
		FluidStack result = recipe.getResultFluid();
		builder.addSlot(RecipeIngredientRole.OUTPUT, 71, 5)
				.setFluidRenderer(gaugeCapacity(result.getAmount()), false, 16, 32)
				.addIngredient(NeoForgeTypes.FLUID_STACK, result);
		if (recipe.byproduct().isPresent()) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 71, 43).addItemStack(recipe.getByproduct());
		}
	}
}
