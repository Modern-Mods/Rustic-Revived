package nadiendev.rusticrevived.block.decor;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * Dispenser behaviour of rope (legacy DispenseRope): a dispenser facing a rope, directly or across one block
 * of air, extends that rope downwards. Otherwise the rope item is dropped as usual.
 */
public class RopeDispenseBehavior extends DefaultDispenseItemBehavior {
	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		Level level = source.level();
		Direction facing = source.state().getValue(DispenserBlock.FACING);
		BlockPos front = source.pos().relative(facing);
		RopeBlock rope = ModBlocks.ROPE.get();
		boolean extended;
		if (level.getBlockState(front).is(rope)) {
			extended = rope.extendDownwards(level, front);
		} else {
			BlockPos beyond = front.relative(facing);
			extended = level.getBlockState(front).isAir() && level.getBlockState(beyond).is(rope) && rope.extendDownwards(level, beyond);
		}
		if (extended) {
			stack.shrink(1);
			return stack;
		}
		return super.execute(source, stack);
	}
}
