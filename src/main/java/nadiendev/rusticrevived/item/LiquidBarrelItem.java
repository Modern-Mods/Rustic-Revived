package nadiendev.rusticrevived.item;

import java.util.List;

import nadiendev.rusticrevived.blockentity.alchemy.LiquidBarrelBlockEntity;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

/**
 * Liquid barrel item keeping its fluid in the fluid component (copied to and from the block
 * entity), exposes an item fluid handler.
 */
public class LiquidBarrelItem extends BlockItem {
	public LiquidBarrelItem(Block block, Properties properties) {
		super(block, properties);
	}

	public static FluidStack getFluid(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.FLUID, SimpleFluidContent.EMPTY).copy();
	}

	/** A liquid barrel item holding the fluid. */
	public static ItemStack filled(ItemStack barrel, FluidStack fluid) {
		ItemStack stack = barrel.copyWithCount(1);
		stack.set(ModDataComponents.FLUID, SimpleFluidContent.copyOf(fluid));
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		FluidStack fluid = getFluid(stack);
		if (!fluid.isEmpty()) {
			tooltip.add(Component.translatable("tooltip.rusticrevived.liquid_barrel", fluid.getAmount(), fluid.getHoverName())
					.withStyle(ChatFormatting.GRAY));
		}
	}

	/** Item fluid handler with the barrel capacity, refusing gaseous and hot fluids. */
	public static class FluidHandler extends FluidHandlerItemStack {
		public FluidHandler(ItemStack container) {
			super(ModDataComponents.FLUID, container, LiquidBarrelBlockEntity.CAPACITY);
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			return LiquidBarrelBlockEntity.canHold(fluid);
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return LiquidBarrelBlockEntity.canHold(stack);
		}
	}
}
