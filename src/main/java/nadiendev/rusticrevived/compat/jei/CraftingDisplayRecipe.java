package nadiendev.rusticrevived.compat.jei;

import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Display-only crafting recipe showing a dynamic Rustic crafting recipe (olive oiling, vanta
 * oiling, cabinets) in JEI's crafting category (legacy OliveOilRecipeWrapper, VantaOilRecipeWrapper,
 * CabinetRecipeWrapper). It never matches a real crafting grid.
 *
 * @param inputs       stacks cycled in each grid slot (row major for shaped displays)
 * @param outputs      stacks cycled in the output slot
 * @param width        grid width, 0 for shapeless displays
 * @param height       grid height, 0 for shapeless displays
 * @param linkedInputs grid slots cycling in sync with the output (same list size as outputs);
 *                     shapeless displays may only link slot 0
 */
public class CraftingDisplayRecipe extends CustomRecipe {
	private final List<List<ItemStack>> inputs;
	private final List<ItemStack> outputs;
	private final int width;
	private final int height;
	private final int[] linkedInputs;
	private final RecipeSerializer<?> serializer;

	public CraftingDisplayRecipe(RecipeSerializer<?> serializer, List<List<ItemStack>> inputs, List<ItemStack> outputs, int width, int height,
			int... linkedInputs) {
		super(CraftingBookCategory.MISC);
		this.serializer = serializer;
		this.inputs = inputs;
		this.outputs = outputs;
		this.width = width;
		this.height = height;
		this.linkedInputs = linkedInputs;
	}

	public List<List<ItemStack>> inputs() {
		return inputs;
	}

	public List<ItemStack> outputs() {
		return outputs;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public boolean isShapeless() {
		return width <= 0 || height <= 0;
	}

	public int[] linkedInputs() {
		return linkedInputs;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int gridWidth, int gridHeight) {
		return isShapeless() ? inputs.size() <= gridWidth * gridHeight : width <= gridWidth && height <= gridHeight;
	}

	/** The serializer of the real dynamic recipe (a simple crafting serializer, only the category is encoded). */
	@Override
	public RecipeSerializer<?> getSerializer() {
		return serializer;
	}
}
