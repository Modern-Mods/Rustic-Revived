package nadiendev.rusticrevived.recipe;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Contents of an alchemic condenser.
 *
 * @param ingredients    the ingredient slots (2 for the basic condenser, 3 for the advanced one)
 * @param modifier       the modifier slot, {@link ItemStack#EMPTY} for the basic condenser
 * @param bottle         the bottle slot
 * @param fluid          the condenser tank
 * @param advancedDevice whether the device is an advanced condenser (basic condensers only accept
 *                       non-advanced recipes)
 */
public record CondenserRecipeInput(List<ItemStack> ingredients, ItemStack modifier, ItemStack bottle, FluidStack fluid,
		boolean advancedDevice) implements RecipeInput {

	@Override
	public ItemStack getItem(int index) {
		if (index < ingredients.size()) return ingredients.get(index);
		if (index == ingredients.size()) return modifier;
		if (index == ingredients.size() + 1) return bottle;
		return ItemStack.EMPTY;
	}

	@Override
	public int size() {
		return ingredients.size() + 2;
	}
}
