package nadiendev.rusticrevived.block.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Right-click behaviours shared by the alchemy machines.
 */
public final class AlchemyInteractions {
	private AlchemyInteractions() {
	}

	/** Whether the stack can hold fluids (bucket, fluid bottle, liquid barrel, other mods' tanks...). */
	public static boolean isFluidContainer(ItemStack stack) {
		return !stack.isEmpty() && FluidUtil.getFluidHandler(stack.copyWithCount(1)).isPresent();
	}

	/**
	 * Fills or empties the held fluid container with the tank. The client always reports a
	 * success, the server does the transfer.
	 *
	 * @return whether the interaction happened
	 */
	public static boolean useFluidContainer(Level level, Player player, InteractionHand hand, IFluidHandler tank) {
		return level.isClientSide || FluidUtil.interactWithFluidHandler(player, hand, tank);
	}

	/** {@link #useFluidContainer} result that always consumes the click (no bucket placing behind the machine). */
	public static ItemInteractionResult useFluidContainerConsuming(Level level, Player player, InteractionHand hand, IFluidHandler tank) {
		useFluidContainer(level, player, hand, tank);
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	/** Empties the tank, destroying its content (sneak-use with an empty hand). */
	public static InteractionResult voidTank(Level level, BlockPos pos, IFluidHandler tank) {
		if (!level.isClientSide) {
			FluidStack drained = tank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
			if (!drained.isEmpty()) {
				SoundEvent sound = drained.getFluidType().getSound(drained, SoundActions.BUCKET_EMPTY);
				level.playSound(null, pos, sound != null ? sound : SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1F, 1F);
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	/** Gives the stack of the machine slot to the player (dropped at their feet). */
	public static InteractionResult takeItem(Level level, Player player, IItemHandlerModifiable items, int slot) {
		if (!level.isClientSide) {
			ItemStack stack = items.getStackInSlot(slot);
			items.setStackInSlot(slot, ItemStack.EMPTY);
			ItemEntity entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
			entity.setNoPickUpDelay();
			level.addFreshEntity(entity);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
