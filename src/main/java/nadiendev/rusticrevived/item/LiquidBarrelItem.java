package nadiendev.rusticrevived.item;

import java.util.function.Consumer;

import nadiendev.rusticrevived.blockentity.alchemy.LiquidBarrelBlockEntity;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Liquid barrel item keeping its fluid in the fluid component (copied to and from the block
 * entity), exposes an item fluid handler.
 */
public class LiquidBarrelItem extends BlockItem {
	public LiquidBarrelItem(Block block, Properties properties) {
		super(block, properties);
	}

	public static FluidStack getFluid(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.FLUID.get(), SimpleFluidContent.EMPTY).copy();
	}

	/** A liquid barrel item holding the fluid. */
	public static ItemStack filled(ItemStack barrel, FluidStack fluid) {
		ItemStack stack = barrel.copyWithCount(1);
		stack.set(ModDataComponents.FLUID.get(), SimpleFluidContent.copyOf(fluid));
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		FluidStack fluid = getFluid(stack);
		if (!fluid.isEmpty()) {
			tooltip.accept(Component.translatable("tooltip.rusticrevived.liquid_barrel", fluid.getAmount(), fluid.getHoverName())
					.withStyle(ChatFormatting.GRAY));
		}
	}

	/** Item fluid handler with the barrel capacity, refusing gaseous and hot fluids; emptied barrels lose the component. */
	public static class FluidHandler extends ItemAccessFluidHandler {
		public FluidHandler(ItemAccess itemAccess) {
			super(itemAccess, ModDataComponents.FLUID.get(), LiquidBarrelBlockEntity.CAPACITY);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			return super.isValid(index, resource) && LiquidBarrelBlockEntity.canHold(resource.toStack(1));
		}

		@Override
		protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
			return newAmount <= 0 ? accessResource.without(component) : super.update(accessResource, index, newResource, newAmount);
		}
	}
}
