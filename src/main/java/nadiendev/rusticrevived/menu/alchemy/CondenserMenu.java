package nadiendev.rusticrevived.menu.alchemy;

import nadiendev.rusticrevived.blockentity.alchemy.AbstractCondenserBlockEntity;
import nadiendev.rusticrevived.blockentity.alchemy.CondenserBlockEntity;
import nadiendev.rusticrevived.registry.ModMenus;
import nadiendev.rusticrevived.blockentity.alchemy.AlchemyItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Basic condenser menu (legacy ContainerCondenser): two ingredient slots.
 */
public class CondenserMenu extends AbstractCondenserMenu {
	/** Client side constructor used by the menu type. */
	public CondenserMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
		this(containerId, playerInventory, readBlockEntity(playerInventory, extraData.readBlockPos(), CondenserBlockEntity.class),
				new SimpleContainerData(AbstractCondenserBlockEntity.DATA_COUNT));
	}

	public CondenserMenu(int containerId, Inventory playerInventory, CondenserBlockEntity condenser, ContainerData data) {
		super(ModMenus.CONDENSER.get(), containerId, playerInventory, condenser, data);
	}

	@Override
	protected void addIngredientSlots(AlchemyItems items) {
		addSlot(new ResourceHandlerSlot(items, items::set, AbstractCondenserBlockEntity.SLOT_INGREDIENTS_START, 27, 23));
		addSlot(new ResourceHandlerSlot(items, items::set, AbstractCondenserBlockEntity.SLOT_INGREDIENTS_START + 1, 27, 47));
	}
}
