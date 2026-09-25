package nadiendev.rusticrevived.compat.jei;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Alchemic condenser recipes. The simple category shows the basic recipes (two ingredients, no
 * modifier; they work in both condensers), the advanced category the recipes that need the
 * advanced condenser (three ingredients and a modifier slot).
 */
public class AlchemyCategory extends TexturedRecipeCategory<RecipeHolder<CondenserRecipe>> {
	private final boolean advanced;
	private final IDrawableAnimated flame;
	private final IDrawableStatic arrowSprite;
	/** Progress arrows keyed by brewing time, so the animation lasts as long as the recipe. */
	private final Map<Integer, IDrawableAnimated> arrows = new HashMap<>();

	public AlchemyCategory(IGuiHelper guiHelper, boolean advanced) {
		super(guiHelper, advanced ? JeiRecipeTypes.ADVANCED_ALCHEMY : JeiRecipeTypes.SIMPLE_ALCHEMY,
				advanced ? "jei.rusticrevived.recipe.advanced_alchemy" : "jei.rusticrevived.recipe.simple_alchemy",
				advanced ? ModBlocks.CONDENSER_ADVANCED : ModBlocks.CONDENSER,
				advanced ? "jei_alchemy_advanced" : "jei_alchemy_simple", 130, 79);
		this.advanced = advanced;
		this.flame = guiHelper.createAnimatedDrawable(sprite(130, 0, 14, 14), 300, IDrawableAnimated.StartDirection.TOP, true);
		this.arrowSprite = sprite(130, 14, 50, advanced ? 53 : 28);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CondenserRecipe> holder, IFocusGroup focuses) {
		CondenserRecipe recipe = holder.value();
		if (!recipe.bottle().isEmpty()) {
			builder.addSlot(RecipeIngredientRole.INPUT, 82, 4).addIngredients(recipe.bottle());
		}
		List<Ingredient> ingredients = recipe.ingredients();
		// ingredient slots from the bottom up, like the legacy layout
		int[] slotY = advanced ? new int[] {56, 32, 8} : new int[] {44, 20};
		for (int i = 0; i < ingredients.size() && i < slotY.length; i++) {
			builder.addSlot(RecipeIngredientRole.INPUT, 4, slotY[i]).addIngredients(ingredients.get(i));
		}
		if (advanced) {
			recipe.modifier().ifPresent(modifier -> builder.addSlot(RecipeIngredientRole.INPUT, 43, 4).addIngredients(modifier));
		}
		int amount = recipe.fluid().amount();
		builder.addSlot(RecipeIngredientRole.INPUT, 110, 24)
				.setFluidRenderer(gaugeCapacity(amount), false, 16, 32)
				.addIngredients(NeoForgeTypes.FLUID_STACK, fluidStacks(recipe.fluid().ingredient(), amount));
		builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 32).addItemStack(recipe.getResultStack());
	}

	@Override
	public void draw(RecipeHolder<CondenserRecipe> holder, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
		super.draw(holder, recipeSlotsView, graphics, mouseX, mouseY);
		int time = holder.value().getTime();
		flame.draw(graphics, 44, 43);
		arrows.computeIfAbsent(time, ticks -> guiHelper.createAnimatedDrawable(arrowSprite, ticks, IDrawableAnimated.StartDirection.LEFT, false))
				.draw(graphics, 21, advanced ? 14 : 26);
		drawRightAligned(graphics, duration(time), 128, 68);
	}
}
