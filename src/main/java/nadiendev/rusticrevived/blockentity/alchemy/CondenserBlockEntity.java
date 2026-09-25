package nadiendev.rusticrevived.blockentity.alchemy;

import nadiendev.rusticrevived.menu.alchemy.CondenserMenu;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Basic condenser (legacy TileEntityCondenser): two ingredient slots, basic recipes only.
 */
public class CondenserBlockEntity extends AbstractCondenserBlockEntity {
	public static final int SLOT_COUNT = 5;

	public CondenserBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CONDENSER.get(), pos, state, SLOT_COUNT);
	}

	@Override
	protected int firstIngredientSlot() {
		return SLOT_INGREDIENTS_START;
	}

	@Override
	protected int modifierSlot() {
		return -1;
	}

	@Override
	public boolean isAdvanced() {
		return false;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.rusticrevived.condenser");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new CondenserMenu(containerId, playerInventory, this, getData());
	}
}
