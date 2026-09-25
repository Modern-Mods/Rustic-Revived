package nadiendev.rusticrevived.block.farm;

import java.util.Optional;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Small helpers shared by the Rustic crops: harvesting into the player's inventory, rope axis
 * lookups and "crop" soil checks (farmland or anything that explicitly sustains the plant).
 */
public final class FarmHelper {
	private FarmHelper() {
	}

	/** Gives the stack to the player, dropping it at {@code dropPos} when the inventory is full. */
	public static void giveOrDrop(Player player, Level level, BlockPos dropPos, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			Block.popResource(level, dropPos, stack);
		}
	}

	/** Horizontal axis of a rope block, empty for anything else (or a hanging rope). */
	public static Optional<Direction.Axis> horizontalRopeAxis(BlockState state) {
		if (state.is(ModBlocks.ROPE.get()) && state.hasProperty(BlockStateProperties.AXIS)) {
			Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
			if (axis.isHorizontal()) {
				return Optional.of(axis);
			}
		}
		return Optional.empty();
	}

	/** A rope block oriented along the given axis. */
	public static BlockState rope(Direction.Axis axis) {
		BlockState rope = ModBlocks.ROPE.get().defaultBlockState();
		return rope.hasProperty(BlockStateProperties.AXIS) ? rope.setValue(BlockStateProperties.AXIS, axis) : rope;
	}

	/**
	 * Legacy {@code EnumPlantType.Crop}: farmland, or any soil that explicitly sustains the plant
	 * (fertile soil).
	 */
	public static boolean isCropSoil(BlockState soil, BlockGetter level, BlockPos soilPos, BlockState plant) {
		TriState decision = soil.canSustainPlant(level, soilPos, Direction.UP, plant);
		return decision.isDefault() ? soil.getBlock() instanceof FarmlandBlock : decision.isTrue();
	}
}
