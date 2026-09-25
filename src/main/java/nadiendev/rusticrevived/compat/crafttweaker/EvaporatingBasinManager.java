package nadiendev.rusticrevived.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.natives.ingredient.ExpandCTFluidIngredientNeoForge;

import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Drying basin recipes ({@code <recipetype:rusticrevived:evaporating_basin>}), legacy
 * {@code mods.rustic.EvaporatingBasin}:
 *
 * <pre>
 * &lt;recipetype:rusticrevived:evaporating_basin&gt;.addRecipe(&lt;item:minecraft:sugar&gt;, &lt;fluid:rusticrevived:olive_oil&gt; * 750);
 * &lt;recipetype:rusticrevived:evaporating_basin&gt;.addRecipe("oil_sugar", &lt;item:minecraft:sugar&gt;, &lt;fluid:rusticrevived:olive_oil&gt; * 750, 400);
 * &lt;recipetype:rusticrevived:evaporating_basin&gt;.removeRecipe(&lt;fluid:rusticrevived:ironberry_juice&gt;);
 * </pre>
 *
 * The time defaults to the fluid amount in ticks (legacy behaviour).
 */
@ZenRegister
@ZenCodeType.Name("mods.rusticrevived.EvaporatingBasin")
public final class EvaporatingBasinManager implements IRecipeManager<EvaporatingBasinRecipe> {
	public static final EvaporatingBasinManager INSTANCE = new EvaporatingBasinManager();
	private static final int MIN_TIME = 20;

	private EvaporatingBasinManager() {
	}

	/** Adds a recipe drying {@code input} into {@code output} in {@code time} ticks (0 = the fluid amount). */
	@ZenCodeType.Method
	public void addRecipe(String name, IItemStack output, CTFluidIngredient input, @ZenCodeType.OptionalInt(0) int time) {
		if (time != 0 && time < MIN_TIME) {
			throw new IllegalArgumentException("Minimum evaporation time for the evaporating basin is " + MIN_TIME + " ticks");
		}
		EvaporatingBasinRecipe recipe = new EvaporatingBasinRecipe(ExpandCTFluidIngredientNeoForge.asSizedFluidIngredient(input),
				CrTHelper.stack(output), time);
		CrTHelper.add(this, name, recipe, output.getCommandString());
	}

	/** Legacy signature: the recipe name is generated. */
	@ZenCodeType.Method
	public void addRecipe(IItemStack output, CTFluidIngredient input, @ZenCodeType.OptionalInt(0) int time) {
		addRecipe(CrTHelper.autoName("evaporating_basin", output.getCommandString(), input.getCommandString(), time), output, input, time);
	}

	/** Removes every recipe producing {@code output}. */
	@ZenCodeType.Method
	public void removeRecipe(IItemStack output) {
		remove(output);
	}

	/** Removes every recipe drying the {@code input} fluid. */
	@ZenCodeType.Method
	public void removeRecipe(IFluidStack input) {
		FluidStack fluid = CrTHelper.fluid(input);
		removeMatching(holder -> holder.value().fluid().ingredient().test(fluid));
	}

	@Override
	public RecipeType<EvaporatingBasinRecipe> getRecipeType() {
		return ModRecipes.EVAPORATING_BASIN.get();
	}
}
