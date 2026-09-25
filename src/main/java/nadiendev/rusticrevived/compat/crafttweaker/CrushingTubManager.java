package nadiendev.rusticrevived.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;

import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Crushing tub recipes ({@code <recipetype:rusticrevived:crushing_tub>}), legacy
 * {@code mods.rustic.CrushingTub}:
 *
 * <pre>
 * &lt;recipetype:rusticrevived:crushing_tub&gt;.addRecipe(&lt;fluid:minecraft:water&gt; * 125, null, &lt;item:minecraft:melon&gt;);
 * &lt;recipetype:rusticrevived:crushing_tub&gt;.addRecipe("melon_water", &lt;fluid:minecraft:water&gt; * 125, &lt;item:minecraft:melon&gt;);
 * &lt;recipetype:rusticrevived:crushing_tub&gt;.removeRecipe(&lt;item:rusticrevived:olives&gt;);
 * </pre>
 */
@ZenRegister
@ZenCodeType.Name("mods.rusticrevived.CrushingTub")
public final class CrushingTubManager implements IRecipeManager<CrushingTubRecipe> {
	public static final CrushingTubManager INSTANCE = new CrushingTubManager();

	private CrushingTubManager() {
	}

	/** Adds a recipe squeezing {@code output} (and an optional byproduct) out of {@code input}. */
	@ZenCodeType.Method
	public void addRecipe(String name, IFluidStack output, IIngredient input, @ZenCodeType.Optional IItemStack byproduct) {
		FluidStack fluid = CrTHelper.fluid(output);
		CrTHelper.add(this, name, new CrushingTubRecipe(CrTHelper.ingredient(input), fluid, CrTHelper.stackOrEmpty(byproduct)),
				output.getCommandString());
	}

	/** Legacy signature: {@code byproduct} may be null; the recipe name is generated. */
	@ZenCodeType.Method
	public void addRecipe(IFluidStack output, @ZenCodeType.Nullable IItemStack byproduct, IIngredient input) {
		String name = CrTHelper.autoName("crushing_tub", output.getCommandString(), byproduct == null ? "" : byproduct.getCommandString(),
				input.getCommandString());
		addRecipe(name, output, input, byproduct);
	}

	/** Removes every recipe accepting {@code input}. */
	@ZenCodeType.Method
	public void removeRecipe(IItemStack input) {
		ItemStack stack = CrTHelper.stack(input);
		removeMatching(holder -> holder.value().ingredient().test(stack));
	}

	/** Removes the recipes producing the {@code output} fluid, optionally only those accepting {@code input}. */
	@ZenCodeType.Method
	public void removeRecipe(IFluidStack output, @ZenCodeType.Optional IItemStack input) {
		ItemStack stack = input == null ? null : CrTHelper.stack(input);
		removeMatching(holder -> holder.value().result().getFluid() == output.getFluid()
				&& (stack == null || holder.value().ingredient().test(stack)));
	}

	@Override
	public void removeByInput(IItemStack input) {
		removeRecipe(input);
	}

	@Override
	public RecipeType<CrushingTubRecipe> getRecipeType() {
		return ModRecipes.CRUSHING_TUB.get();
	}
}
