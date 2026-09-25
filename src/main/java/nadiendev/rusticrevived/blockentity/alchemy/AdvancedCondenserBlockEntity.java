package nadiendev.rusticrevived.blockentity.alchemy;

import nadiendev.rusticrevived.menu.alchemy.AdvancedCondenserMenu;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Advanced condenser (legacy TileEntityCondenserAdvancedBottom): a modifier slot and three
 * ingredient slots, runs basic and advanced recipes. The top half has no block entity, its
 * capabilities proxy this one.
 */
public class AdvancedCondenserBlockEntity extends AbstractCondenserBlockEntity {
	public static final int SLOT_MODIFIER = SLOT_INGREDIENTS_START;
	public static final int SLOT_COUNT = 7;

	public AdvancedCondenserBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CONDENSER_ADVANCED.get(), pos, state, SLOT_COUNT);
	}

	@Override
	protected int firstIngredientSlot() {
		return SLOT_MODIFIER + 1;
	}

	@Override
	protected int modifierSlot() {
		return SLOT_MODIFIER;
	}

	@Override
	public boolean isAdvanced() {
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.rusticrevived.condenser_advanced");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new AdvancedCondenserMenu(containerId, playerInventory, this, getData());
	}
}
