package nadiendev.rusticrevived.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.natives.ingredient.ExpandCTFluidIngredientNeoForge;

import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Brewing barrel recipes ({@code <recipetype:rusticrevived:brewing>}), new in Rustic Revived:
 *
 * <pre>
 * &lt;recipetype:rusticrevived:brewing&gt;.addRecipe(&lt;fluid:rusticrevived:wine&gt;, &lt;fluid:rusticrevived:grape_juice&gt;);
 * &lt;recipetype:rusticrevived:brewing&gt;.addRecipe("golden_ambrosia", &lt;fluid:rusticrevived:ambrosia&gt;, &lt;fluid:rusticrevived:golden_apple_juice&gt;, "ambrosia");
 * &lt;recipetype:rusticrevived:brewing&gt;.removeRecipe(&lt;fluid:rusticrevived:cider&gt;);
 * </pre>
 *
 * The barrel converts the input one mB at a time; {@code quality} is "standard" (random 5% - 75%)
 * or "ambrosia" (49% - 74%, cultures never degrade).
 */
@ZenRegister
@ZenCodeType.Name("mods.rusticrevived.Brewing")
public final class BrewingManager implements IRecipeManager<BrewingRecipe> {
	public static final BrewingManager INSTANCE = new BrewingManager();

	private BrewingManager() {
	}

	@ZenCodeType.Method
	public void addRecipe(String name, IFluidStack output, CTFluidIngredient input, @ZenCodeType.OptionalString("standard") String quality) {
		BrewingRecipe.QualityMode mode = null;
		for (BrewingRecipe.QualityMode value : BrewingRecipe.QualityMode.values()) {
			if (value.getSerializedName().equals(quality)) mode = value;
		}
		if (mode == null) {
			throw new IllegalArgumentException("Unknown brewing quality mode '" + quality + "', expected 'standard' or 'ambrosia'");
		}
		BrewingRecipe recipe = new BrewingRecipe(ExpandCTFluidIngredientNeoForge.asFluidIngredient(input), CrTHelper.fluid(output), mode);
		CrTHelper.add(this, name, recipe, output.getCommandString());
	}

	/** Legacy style signature; the recipe name is generated. */
	@ZenCodeType.Method
	public void addRecipe(IFluidStack output, CTFluidIngredient input) {
		addRecipe(CrTHelper.autoName("brewing", output.getCommandString(), input.getCommandString()), output, input, "standard");
	}

	/** Removes every recipe brewing the {@code output} fluid. */
	@ZenCodeType.Method
	public void removeRecipe(IFluidStack output) {
		removeMatching(holder -> holder.value().result().getFluid() == output.getFluid());
	}

	/** Removes every recipe brewing the {@code input} fluid. */
	@ZenCodeType.Method
	public void removeByInputFluid(IFluidStack input) {
		FluidStack fluid = CrTHelper.fluid(input);
		removeMatching(holder -> holder.value().input().test(fluid));
	}

	@Override
	public RecipeType<BrewingRecipe> getRecipeType() {
		return ModRecipes.BREWING.get();
	}
}
