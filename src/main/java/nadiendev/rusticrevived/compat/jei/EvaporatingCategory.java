package nadiendev.rusticrevived.compat.jei;

import java.util.Arrays;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Drying (evaporating) basin: fluid -> item, with the drying time.
 */
public class EvaporatingCategory extends TexturedRecipeCategory<RecipeHolder<EvaporatingBasinRecipe>> {

	public EvaporatingCategory(IGuiHelper guiHelper) {
		super(guiHelper, JeiRecipeTypes.EVAPORATING, "jei.rusticrevived.recipe.evaporating", ModBlocks.EVAPORATING_BASIN, "jei_evaporating_basin", 108, 64);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<EvaporatingBasinRecipe> holder, IFocusGroup focuses) {
		EvaporatingBasinRecipe recipe = holder.value();
		builder.addSlot(RecipeIngredientRole.INPUT, 21, 16)
				.setFluidRenderer(gaugeCapacity(recipe.getAmount()), false, 16, 32)
				.addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(recipe.fluid().getFluids()));
		builder.addSlot(RecipeIngredientRole.OUTPUT, 71, 24).addItemStack(recipe.result());
	}

	@Override
	public void draw(RecipeHolder<EvaporatingBasinRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		super.draw(holder, recipeSlotsView, guiGraphics, mouseX, mouseY);
		drawCentered(guiGraphics, duration(holder.value().getTime()), 54, 54);
	}
}
