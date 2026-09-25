package nadiendev.rusticrevived.recipe;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.item.FluidBottleItem;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModRecipes;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Olive oiling (legacy RecipeOliveOil): any food + an olive oil bottle -> the food with the
 * olive_oiled component (more filling). The bottle comes back empty. Foods can be excluded with
 * the {@link ModTags.Items#OLIVE_OIL_BLACKLIST} tag or the config black/whitelist.
 */
public class OliveOilRecipe extends CustomRecipe {
	public OliveOilRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return findFood(input) >= 0;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		int food = findFood(input);
		if (food < 0) {
			return ItemStack.EMPTY;
		}
		ItemStack result = input.getItem(food).copyWithCount(1);
		result.set(ModDataComponents.OLIVE_OILED, Unit.INSTANCE);
		return result;
	}

	/** Slot of the food if the grid holds exactly one oilable food and one olive oil bottle, else -1. */
	private static int findFood(CraftingInput input) {
		int food = -1;
		boolean oil = false;
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;
			if (food < 0 && canBeOiled(stack)) {
				food = i;
			} else if (!oil && isOliveOilBottle(stack)) {
				oil = true;
			} else {
				return -1;
			}
		}
		return oil ? food : -1;
	}

	public static boolean isOliveOilBottle(ItemStack stack) {
		return stack.getItem() instanceof FluidBottleItem && FluidBottleItem.getFluid(stack).is(ModTags.Fluids.OLIVE_OIL);
	}

	/** Whether the stack is a food that is not oiled yet and allowed by the tag and the config. */
	public static boolean canBeOiled(ItemStack stack) {
		if (stack.getFoodProperties(null) == null || stack.has(ModDataComponents.OLIVE_OILED) || stack.is(ModTags.Items.OLIVE_OIL_BLACKLIST)) {
			return false;
		}
		String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		return RusticConfig.COMMON.oliveOilUseWhitelist.get() == RusticConfig.COMMON.oliveOilBlacklist.get().contains(id);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.OLIVE_OIL_SERIALIZER.get();
	}
}
