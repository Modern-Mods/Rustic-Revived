package nadiendev.rusticrevived.menu.alchemy;

import nadiendev.rusticrevived.blockentity.alchemy.AbstractCondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.AdvancedCondenserBlockEntity;
import nadiendev.rusticrevived.registry.ModMenus;
import nadiendev.rusticrevived.blockentity.alchemy.AlchemyItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Advanced condenser menu (legacy ContainerCondenserAdvanced): a modifier slot and three
 * ingredient slots.
 */
public class AdvancedCondenserMenu extends AbstractCondenserMenu {
	/** Client side constructor used by the menu type. */
	public AdvancedCondenserMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, readBlockEntity(playerInventory, extraData.readBlockPos(), AdvancedCondenserBlockEntity.class),
				new SimpleContainerData(AbstractCondenserBlockEntity.DATA_COUNT));
	}

	public AdvancedCondenserMenu(int containerId, Inventory playerInventory, AdvancedCondenserBlockEntity condenser, ContainerData data) {
		super(ModMenus.CONDENSER_ADVANCED.get(), containerId, playerInventory, condenser, data);
	}

	@Override
	protected void addIngredientSlots(AlchemyItems items) {
		addSlot(new ResourceHandlerSlot(items, items::set, AdvancedCondenserBlockEntity.SLOT_MODIFIER, 66, 7));
		addSlot(new ResourceHandlerSlot(items, items::set, AdvancedCondenserBlockEntity.SLOT_MODIFIER + 1, 27, 11));
		addSlot(new ResourceHandlerSlot(items, items::set, AdvancedCondenserBlockEntity.SLOT_MODIFIER + 2, 27, 35));
		addSlot(new ResourceHandlerSlot(items, items::set, AdvancedCondenserBlockEntity.SLOT_MODIFIER + 3, 27, 59));
	}
}
