package nadiendev.rusticrevived.blockentity.storage;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.block.storage.CabinetBlock;
import nadiendev.rusticrevived.menu.storage.CabinetMenu;
import nadiendev.rusticrevived.menu.storage.StorageMenu;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cabinet inventory (legacy TileEntityCabinet): 27 slots, the door animation and the planks
 * {@link #getMaterial() material} it was crafted from (synced to the client for rendering; kept on
 * the item through the {@code cabinet_material} component).
 */
public class CabinetBlockEntity extends StorageBlockEntity {
	private static final String MATERIAL_TAG = "material";
	private static final int DOOR_EVENT = 1;

	private final ChestLidController doorController = new ChestLidController();
	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			playSound(level, pos, state, SoundEvents.CHEST_OPEN);
		}

		@Override
		protected void onClose(Level level, BlockPos pos, BlockState state) {
			playSound(level, pos, state, SoundEvents.CHEST_CLOSE);
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
			level.blockEvent(pos, state.getBlock(), DOOR_EVENT, openCount);
		}

		@Override
		protected boolean isOwnContainer(Player player) {
			return player.containerMenu instanceof StorageMenu menu && menu.isFor(CabinetBlockEntity.this);
		}
	};
	@Nullable
	private Item material;

	public CabinetBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CABINET.get(), pos, state);
	}

	/** Planks the cabinet was made from, {@code null} for the default (mixed woods) texture. */
	@Nullable
	public Item getMaterial() {
		return material;
	}

	public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, CabinetBlockEntity cabinet) {
		cabinet.doorController.tickLid();
	}

	/** Door openness in [0, 1]. */
	public float getOpenness(float partialTick) {
		return doorController.getOpenness(partialTick);
	}

	/** Only the half that renders the door plays the sounds. */
	private static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound) {
		if (state.getValue(CabinetBlock.TOP)) return;
		level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, sound, SoundSource.BLOCKS, 0.5F,
				level.random.nextFloat() * 0.1F + 0.9F);
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		if (id == DOOR_EVENT) {
			doorController.shouldBeOpen(type > 0);
			return true;
		}
		return super.triggerEvent(id, type);
	}

	@Override
	public void startOpen(Player player) {
		if (!remove && !player.isSpectator()) {
			openersCounter.incrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
		}
	}

	@Override
	public void stopOpen(Player player) {
		if (!remove && !player.isSpectator()) {
			openersCounter.decrementOpeners(player, getLevel(), getBlockPos(), getBlockState());
		}
	}

	public void recheckOpen() {
		if (!remove) {
			openersCounter.recheckOpeners(getLevel(), getBlockPos(), getBlockState());
		}
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.rusticrevived.cabinet");
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new CabinetMenu(containerId, inventory, this);
	}

	// ---------------------------------------------------------------- material

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		material = null;
		if (tag.contains(MATERIAL_TAG)) {
			ResourceLocation id = ResourceLocation.tryParse(tag.getString(MATERIAL_TAG));
			Item item = id == null ? Items.AIR : BuiltInRegistries.ITEM.get(id);
			material = item == Items.AIR ? null : item;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		saveMaterial(tag);
	}

	private void saveMaterial(CompoundTag tag) {
		if (material != null) {
			tag.putString(MATERIAL_TAG, BuiltInRegistries.ITEM.getKey(material).toString());
		}
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput componentInput) {
		super.applyImplicitComponents(componentInput);
		material = componentInput.get(ModDataComponents.CABINET_MATERIAL.get());
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if (material != null) {
			components.set(ModDataComponents.CABINET_MATERIAL.get(), material);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void removeComponentsFromTag(CompoundTag tag) {
		super.removeComponentsFromTag(tag);
		tag.remove(MATERIAL_TAG);
	}

	/** The client only needs the material (the inventory is synced by the menu). */
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		saveMaterial(tag);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
