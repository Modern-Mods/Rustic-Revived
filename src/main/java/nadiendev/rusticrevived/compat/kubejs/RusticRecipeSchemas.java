package nadiendev.rusticrevived.compat.kubejs;

import java.util.List;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.EnumComponent;
import dev.latvian.mods.kubejs.recipe.component.FluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.FluidStackComponent;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * KubeJS recipe schemas of Rustic's machines. Keys match the JSON fields documented on the recipe
 * classes; positional arguments are the required keys followed by the optional ones, in order:
 *
 * <pre>
 * event.recipes.rusticrevived.crushing_tub(Fluid.of('rusticrevived:olive_oil', 250), 'rusticrevived:olives'[, byproduct])
 * event.recipes.rusticrevived.evaporating_basin('rusticrevived:tiny_iron_dust', Fluid.of('rusticrevived:ironberry_juice', 500)[, time])
 * event.recipes.rusticrevived.condenser(elixir, [ingredient1, ingredient2, ingredient3][, modifier, bottle, fluid, time, advanced])
 * event.recipes.rusticrevived.brewing(Fluid.of('rusticrevived:wine', 1), 'rusticrevived:grape_juice'[, 'ambrosia'])
 * </pre>
 *
 * Every optional key can also be set with a builder call, e.g. {@code .time(600)}. Condenser
 * recipes with a modifier or more than two ingredients are marked advanced automatically.
 */
public final class RusticRecipeSchemas {
	public static final EnumComponent<BrewingRecipe.QualityMode> QUALITY_COMPONENT =
			EnumComponent.create(RecipeComponentType.key(RusticRevived.id("brewing_quality")), BrewingRecipe.QualityMode.class);

	// crushing tub
	public static final RecipeKey<FluidStack> CRUSHING_RESULT = FluidStackComponent.FLUID_STACK.outputKey("result");
	public static final RecipeKey<Ingredient> CRUSHING_INGREDIENT = IngredientComponent.INGREDIENT.inputKey("ingredient");
	public static final RecipeKey<ItemStack> CRUSHING_BYPRODUCT = ItemStackComponent.ITEM_STACK.outputKey("byproduct").defaultOptional();

	// evaporating basin
	public static final RecipeKey<ItemStack> EVAPORATING_RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
	public static final RecipeKey<SizedFluidIngredient> EVAPORATING_FLUID = SizedFluidIngredientComponent.SIZED_FLUID_INGREDIENT.inputKey("fluid");
	public static final RecipeKey<Integer> EVAPORATING_TIME = NumberComponent.NON_NEGATIVE_INT.otherKey("time").defaultOptional();

	// condenser
	public static final RecipeKey<ItemStack> CONDENSER_RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");
	public static final RecipeKey<List<Ingredient>> CONDENSER_INGREDIENTS = IngredientComponent.INGREDIENT.asList().inputKey("ingredients");
	public static final RecipeKey<Ingredient> CONDENSER_MODIFIER = IngredientComponent.INGREDIENT.inputKey("modifier").defaultOptional();
	public static final RecipeKey<Ingredient> CONDENSER_BOTTLE = IngredientComponent.INGREDIENT.inputKey("bottle").defaultOptional();
	public static final RecipeKey<SizedFluidIngredient> CONDENSER_FLUID = SizedFluidIngredientComponent.SIZED_FLUID_INGREDIENT.inputKey("fluid").defaultOptional();
	public static final RecipeKey<Integer> CONDENSER_TIME = NumberComponent.NON_NEGATIVE_INT.otherKey("time").defaultOptional();
	public static final RecipeKey<Boolean> CONDENSER_ADVANCED = BooleanComponent.BOOLEAN.otherKey("advanced").defaultOptional();

	// brewing barrel
	public static final RecipeKey<FluidStack> BREWING_RESULT = FluidStackComponent.FLUID_STACK.outputKey("result");
	public static final RecipeKey<FluidIngredient> BREWING_INPUT = FluidIngredientComponent.FLUID_INGREDIENT.inputKey("input");
	public static final RecipeKey<BrewingRecipe.QualityMode> BREWING_QUALITY = QUALITY_COMPONENT.otherKey("quality").defaultOptional();

	public static final KubeRecipeFactory CONDENSER_FACTORY = new KubeRecipeFactory(RusticRevived.id("condenser"), CondenserKubeRecipe.class,
			CondenserKubeRecipe::new);

	public static final RecipeSchema CRUSHING_TUB = new RecipeSchema(CRUSHING_RESULT, CRUSHING_INGREDIENT, CRUSHING_BYPRODUCT)
			.uniqueId(CRUSHING_RESULT);
	public static final RecipeSchema EVAPORATING_BASIN = new RecipeSchema(EVAPORATING_RESULT, EVAPORATING_FLUID, EVAPORATING_TIME)
			.uniqueId(EVAPORATING_RESULT);
	public static final RecipeSchema CONDENSER = new RecipeSchema(CONDENSER_RESULT, CONDENSER_INGREDIENTS, CONDENSER_MODIFIER, CONDENSER_BOTTLE,
			CONDENSER_FLUID, CONDENSER_TIME, CONDENSER_ADVANCED)
			.factory(CONDENSER_FACTORY)
			.uniqueId(CONDENSER_RESULT);
	public static final RecipeSchema BREWING = new RecipeSchema(BREWING_RESULT, BREWING_INPUT, BREWING_QUALITY)
			.uniqueId(BREWING_RESULT);

	private RusticRecipeSchemas() {
	}
}
