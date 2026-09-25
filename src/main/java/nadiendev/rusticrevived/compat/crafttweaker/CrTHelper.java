package nadiendev.rusticrevived.compat.crafttweaker;

import java.util.Arrays;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import com.blamejared.crafttweaker.api.util.StringUtil;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Conversions and small helpers shared by Rustic's CraftTweaker recipe managers and handlers.
 */
final class CrTHelper {
	private CrTHelper() {
	}

	static ItemStack stack(IItemStack stack) {
		return stack.getImmutableInternal().copy();
	}

	static ItemStack stackOrEmpty(IItemStack stack) {
		return stack == null ? ItemStack.EMPTY : stack(stack);
	}

	static FluidStack fluid(IFluidStack fluid) {
		return fluid.<FluidStack>getImmutableInternal().copy();
	}

	static Ingredient ingredient(IIngredient ingredient) {
		return ingredient.asVanillaIngredient();
	}

	/**
	 * Recipe name for the legacy add methods, which take no name: a stable hash of the recipe's
	 * command strings, so the same script line always produces the same id.
	 */
	static String autoName(String prefix, Object... parts) {
		return prefix + "_" + Integer.toHexString(Arrays.deepHashCode(parts));
	}

	static <T extends Recipe<?>> void add(IRecipeManager<T> manager, String name, T recipe, String output) {
		RecipeHolder<T> holder = manager.createHolder(manager.fixRecipeId(name), recipe);
		CraftTweakerAPI.apply(new ActionAddRecipe<>(manager, holder).outputDescriber(ignored -> output));
	}

	static String command(ItemStack stack) {
		return ItemStackUtil.getCommandString(stack);
	}

	static String command(FluidStack stack) {
		return IFluidStack.of(stack).getCommandString();
	}

	static String command(Ingredient ingredient) {
		return IIngredient.fromIngredient(ingredient).getCommandString();
	}

	static String quote(RecipeHolder<?> holder) {
		return StringUtil.quoteAndEscape(holder.id());
	}
}
