package nadiendev.rusticrevived.compat.crafttweaker;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.natives.ingredient.ExpandCTFluidIngredientNeoForge;

import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * Alchemic condenser recipes ({@code <recipetype:rusticrevived:condenser>}), legacy
 * {@code mods.rustic.Condenser}:
 *
 * <pre>
 * &lt;recipetype:rusticrevived:condenser&gt;.addRecipe(slownessElixir, &lt;item:minecraft:cobweb&gt;, &lt;item:minecraft:vine&gt;);
 * &lt;recipetype:rusticrevived:condenser&gt;.addRecipe(longSlownessElixir, [&lt;item:minecraft:cobweb&gt;, &lt;item:minecraft:vine&gt;], &lt;item:rusticrevived:horsetail&gt;);
 * &lt;recipetype:rusticrevived:condenser&gt;.addRecipe("long_slowness", longSlownessElixir, [&lt;item:minecraft:cobweb&gt;, &lt;item:minecraft:vine&gt;], &lt;item:rusticrevived:horsetail&gt;);
 * &lt;recipetype:rusticrevived:condenser&gt;.removeRecipe(slownessElixir);
 * </pre>
 *
 * A recipe is advanced (advanced condenser only) when it has a modifier or more than two inputs
 * (null inputs count, which forces a two ingredient recipe to be advanced) or when
 * {@code advanced} is true. Optional arguments: {@code bottle} defaults to a glass bottle
 * ({@code <item:minecraft:air>} = no bottle), {@code fluid} to 125 mB of water and {@code time}
 * (ticks) to 400 for basic and 300 for advanced recipes.
 */
@ZenRegister
@ZenCodeType.Name("mods.rusticrevived.Condenser")
public final class CondenserManager implements IRecipeManager<CondenserRecipe> {
	public static final CondenserManager INSTANCE = new CondenserManager();
	private static final int MAX_INPUTS = 3;

	private CondenserManager() {
	}

	@ZenCodeType.Method
	public void addRecipe(String name, IItemStack output, IIngredient[] inputs, @ZenCodeType.Optional @ZenCodeType.Nullable IIngredient modifier,
			@ZenCodeType.Optional @ZenCodeType.Nullable IIngredient bottle, @ZenCodeType.Optional @ZenCodeType.Nullable CTFluidIngredient fluid,
			@ZenCodeType.OptionalInt(0) int time, @ZenCodeType.OptionalBoolean(false) boolean advanced) {
		if (inputs.length > MAX_INPUTS) {
			throw new IllegalArgumentException("Condenser recipes have at most " + MAX_INPUTS + " inputs");
		}
		if (time < 0) {
			throw new IllegalArgumentException("Brew time must be positive");
		}
		List<Ingredient> ingredients = Arrays.stream(inputs).filter(Objects::nonNull).map(CrTHelper::ingredient)
				.filter(ingredient -> !ingredient.isEmpty()).toList();
		if (ingredients.isEmpty()) {
			throw new IllegalArgumentException("Condenser recipes need at least one input");
		}
		boolean isAdvanced = advanced || modifier != null || inputs.length > 2;
		Ingredient bottleIngredient = bottle == null ? CondenserRecipe.defaultBottle() : CrTHelper.ingredient(bottle);
		SizedFluidIngredient fluidIngredient = fluid == null ? CondenserRecipe.defaultFluid() : ExpandCTFluidIngredientNeoForge.asSizedFluidIngredient(fluid);
		CondenserRecipe recipe = new CondenserRecipe(isAdvanced, ingredients, Optional.ofNullable(modifier).map(CrTHelper::ingredient),
				bottleIngredient, fluidIngredient, time, CrTHelper.stack(output));
		CrTHelper.add(this, name, recipe, output.getCommandString());
	}

	/** Legacy simple recipe: two inputs, no modifier. */
	@ZenCodeType.Method
	public void addRecipe(IItemStack output, IIngredient input1, IIngredient input2) {
		addRecipe(output, new IIngredient[] {input1, input2}, null, null, null, 0);
	}

	/** Legacy full signature; the recipe name is generated. */
	@ZenCodeType.Method
	public void addRecipe(IItemStack output, IIngredient[] inputs, @ZenCodeType.Optional @ZenCodeType.Nullable IIngredient modifier,
			@ZenCodeType.Optional @ZenCodeType.Nullable IIngredient bottle, @ZenCodeType.Optional @ZenCodeType.Nullable CTFluidIngredient fluid,
			@ZenCodeType.OptionalInt(0) int time) {
		String name = CrTHelper.autoName("condenser", output.getCommandString(),
				Arrays.stream(inputs).map(input -> input == null ? "null" : input.getCommandString()).toArray(),
				modifier == null ? "" : modifier.getCommandString(), bottle == null ? "" : bottle.getCommandString(),
				fluid == null ? "" : fluid.getCommandString(), time);
		addRecipe(name, output, inputs, modifier, bottle, fluid, time, false);
	}

	/** Removes every recipe producing {@code output}. */
	@ZenCodeType.Method
	public void removeRecipe(IItemStack output) {
		remove(output);
	}

	@Override
	public RecipeType<CondenserRecipe> getRecipeType() {
		return ModRecipes.CONDENSER.get();
	}
}
