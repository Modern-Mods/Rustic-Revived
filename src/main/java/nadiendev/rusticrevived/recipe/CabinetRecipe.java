package nadiendev.rusticrevived.recipe;

import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModRecipes;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Cabinet (legacy RecipeCabinet): cabinet materials all around, a wooden trapdoor on the left or right
 * side and an empty centre. The result remembers the material in the cabinet_material component when
 * a single kind of planks was used; mixed woods give the default texture.
 */
public class CabinetRecipe extends CustomRecipe {
	public CabinetRecipe(CraftingBookCategory category) {
		super(category);
	}

	private static boolean isMaterial(ItemStack stack) {
		return stack.is(ModTags.Items.CABINET_MATERIALS);
	}

	private static boolean isTrapdoor(ItemStack stack) {
		return stack.is(ItemTags.WOODEN_TRAPDOORS);
	}

	private static ItemStack at(CraftingInput input, int x, int y) {
		return input.getItem(x + y * input.width());
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.width() != 3 || input.height() != 3) return false;
		for (int x = 0; x < 3; x++) {
			if (!isMaterial(at(input, x, 0)) || !isMaterial(at(input, x, 2))) return false;
		}
		if (!at(input, 1, 1).isEmpty()) return false;
		ItemStack left = at(input, 0, 1);
		ItemStack right = at(input, 2, 1);
		return isMaterial(left) && isTrapdoor(right) || isMaterial(right) && isTrapdoor(left);
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		ItemStack result = new ItemStack(ModBlocks.CABINET.get());
		Item material = null;
		for (ItemStack stack : input.items()) {
			if (!isMaterial(stack)) continue;
			if (material != null && material != stack.getItem()) return result;
			material = stack.getItem();
		}
		if (material != null) {
			result.set(ModDataComponents.CABINET_MATERIAL.get(), material);
		}
		return result;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.CABINET_SERIALIZER.get();
	}
}
