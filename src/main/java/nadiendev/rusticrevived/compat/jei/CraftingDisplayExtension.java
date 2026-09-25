package nadiendev.rusticrevived.compat.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Lays out {@link CraftingDisplayRecipe}s in JEI's crafting category, linking the focus of the
 * linked input slots to the output so e.g. the oiled food shown matches the food shown.
 */
public class CraftingDisplayExtension implements ICraftingCategoryExtension<CraftingDisplayRecipe> {

	@Override
	public void setRecipe(RecipeHolder<CraftingDisplayRecipe> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper,
			IFocusGroup focuses) {
		CraftingDisplayRecipe recipe = holder.value();
		List<IRecipeSlotBuilder> inputSlots = craftingGridHelper.createAndSetInputs(builder, recipe.inputs(), recipe.width(), recipe.height());
		IRecipeSlotBuilder outputSlot = craftingGridHelper.createAndSetOutputs(builder, recipe.outputs());
		if (recipe.isShapeless()) {
			builder.setShapeless();
		}
		if (recipe.linkedInputs().length > 0) {
			List<IIngredientAcceptor<?>> linked = new ArrayList<>();
			for (int slot : recipe.linkedInputs()) {
				linked.add(inputSlots.get(slot));
			}
			linked.add(outputSlot);
			builder.createFocusLink(linked.toArray(IIngredientAcceptor[]::new));
		}
	}

	@Override
	public int getWidth(RecipeHolder<CraftingDisplayRecipe> holder) {
		return holder.value().width();
	}

	@Override
	public int getHeight(RecipeHolder<CraftingDisplayRecipe> holder) {
		return holder.value().height();
	}
}
