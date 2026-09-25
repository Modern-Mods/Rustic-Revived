package nadiendev.rusticrevived.compat.jei;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Brewing barrel: input fluid -> booze, with the optional culture tank and the quality range of a
 * brew started without culture.
 */
public class BrewingCategory extends TexturedRecipeCategory<RecipeHolder<BrewingRecipe>> {
	private static final int SHOWN_AMOUNT = 1000;

	private final IDrawableAnimated bubbles;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated cultureArrow;

	public BrewingCategory(IGuiHelper guiHelper) {
		super(guiHelper, JeiRecipeTypes.BREWING, "jei.rusticrevived.recipe.brewing", ModBlocks.BREWING_BARREL, "jei_brewing", 130, 80);
		this.bubbles = guiHelper.createAnimatedDrawable(sprite(130, 0, 11, 28), 100, IDrawableAnimated.StartDirection.BOTTOM, false);
		this.arrow = guiHelper.createAnimatedDrawable(sprite(130, 28, 24, 16), 1200, IDrawableAnimated.StartDirection.LEFT, false);
		this.cultureArrow = guiHelper.createAnimatedDrawable(sprite(130, 44, 10, 10), 1200, IDrawableAnimated.StartDirection.LEFT, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BrewingRecipe> holder, IFocusGroup focuses) {
		BrewingRecipe recipe = holder.value();
		List<FluidStack> inputs = Arrays.stream(recipe.input().getStacks()).map(stack -> stack.copyWithAmount(SHOWN_AMOUNT)).toList();
		builder.addSlot(RecipeIngredientRole.INPUT, 40, 24)
				.setFluidRenderer(SHOWN_AMOUNT, false, 16, 32)
				.addIngredients(NeoForgeTypes.FLUID_STACK, inputs);
		FluidStack result = recipe.result().copyWithAmount(SHOWN_AMOUNT);
		builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 24)
				.setFluidRenderer(SHOWN_AMOUNT, false, 16, 32)
				.addIngredient(NeoForgeTypes.FLUID_STACK, result);
		builder.addSlot(RecipeIngredientRole.CATALYST, 4, 32)
				.setFluidRenderer(SHOWN_AMOUNT, false, 16, 16)
				.addIngredient(NeoForgeTypes.FLUID_STACK, result)
				.addRichTooltipCallback((slotView, tooltip) ->
						tooltip.add(Component.translatable("jei.rusticrevived.recipe.brewing.culture").withStyle(ChatFormatting.GRAY)));
	}

	@Override
	public void draw(RecipeHolder<BrewingRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		super.draw(holder, recipeSlotsView, guiGraphics, mouseX, mouseY);
		bubbles.draw(guiGraphics, 117, 26);
		arrow.draw(guiGraphics, 63, 32);
		cultureArrow.draw(guiGraphics, 23, 35);
		boolean ambrosia = holder.value().quality() == BrewingRecipe.QualityMode.AMBROSIA;
		drawCentered(guiGraphics, Component.translatable("jei.rusticrevived.recipe.brewing.quality", ambrosia ? 49 : 5, ambrosia ? 74 : 75), 65, 66);
	}
}
