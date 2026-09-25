package nadiendev.rusticrevived.compat.jei;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * Elixirs are distinct JEI entries per {@link DataComponents#POTION_CONTENTS potion contents}
 * (base potion and custom effects).
 */
public final class PotionContentsSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
	public static final PotionContentsSubtypeInterpreter INSTANCE = new PotionContentsSubtypeInterpreter();

	private PotionContentsSubtypeInterpreter() {
	}

	@Override
	public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
		PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
		return contents == null || !contents.hasEffects() ? null : contents;
	}
}
