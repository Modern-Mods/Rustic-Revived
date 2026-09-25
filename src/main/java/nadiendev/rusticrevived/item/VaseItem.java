package nadiendev.rusticrevived.item;

import nadiendev.rusticrevived.block.storage.VaseBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;

/**
 * Vase item; its design lives in the block_state component so the placed vase keeps it (scroll
 * while sneaking to change it). The default design carries no component so crafted, creative and
 * broken default vases stack together.
 */
public class VaseItem extends BlockItem {
	public VaseItem(Block block, Properties properties) {
		super(block, properties);
	}

	/** Design of a vase stack (also the "rusticrevived:design" item model predicate). */
	public static int getDesign(ItemStack stack) {
		Integer design = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).get(VaseBlock.DESIGN);
		return design == null ? VaseBlock.MIN_DESIGN : design;
	}

	/** The design {@code offset} steps away from {@code design} in {@link VaseBlock#DESIGN_ORDER}. */
	public static int cycleDesign(int design, int offset) {
		int count = VaseBlock.DESIGN_ORDER.length;
		for (int i = 0; i < count; i++) {
			if (VaseBlock.DESIGN_ORDER[i] == design) {
				return VaseBlock.DESIGN_ORDER[Math.floorMod(i + offset, count)];
			}
		}
		return VaseBlock.MIN_DESIGN;
	}

	/** Server side: sets the design of a vase stack (called from VaseDesignPayload). */
	public void setDesign(ItemStack stack, int design) {
		if (design < VaseBlock.MIN_DESIGN || design > VaseBlock.MAX_DESIGN) return;
		if (design == VaseBlock.MIN_DESIGN) {
			stack.remove(DataComponents.BLOCK_STATE);
		} else {
			stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(VaseBlock.DESIGN, design));
		}
	}
}
