package nadiendev.rusticrevived.compat.jei;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Fluid bottles and liquid barrels are distinct JEI entries per contained fluid
 * ({@link ModDataComponents#FLUID}); amount and booze quality are ignored.
 */
public final class FluidContentSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
	public static final FluidContentSubtypeInterpreter INSTANCE = new FluidContentSubtypeInterpreter();

	private FluidContentSubtypeInterpreter() {
	}

	@Override
	public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
		SimpleFluidContent content = stack.get(ModDataComponents.FLUID);
		return content == null || content.isEmpty() ? null : content.getFluid();
	}

	@Override
	public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
		SimpleFluidContent content = stack.get(ModDataComponents.FLUID);
		return content == null || content.isEmpty() ? "" : BuiltInRegistries.FLUID.getKey(content.getFluid()).toString();
	}
}
