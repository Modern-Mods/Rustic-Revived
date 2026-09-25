package nadiendev.rusticrevived.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Recipe input made of a single fluid (brewing barrel, evaporating basin).
 */
public record FluidRecipeInput(FluidStack fluid) implements RecipeInput {
	@Override
	public ItemStack getItem(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return fluid.isEmpty();
	}
}
