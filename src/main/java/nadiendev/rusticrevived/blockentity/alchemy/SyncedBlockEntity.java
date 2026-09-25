package nadiendev.rusticrevived.blockentity.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Base of the alchemy block entities: their whole state is sent to the client (legacy
 * ITileEntitySyncable / block event 1) whenever {@link #sync()} is called.
 */
public abstract class SyncedBlockEntity extends BlockEntity {

	protected SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** Marks the block entity dirty and sends its data to the watching clients. */
	public void sync() {
		setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	/** Drops every stack of the given handler at this block (used when the block is broken). */
	protected void dropContents(IItemHandler handler) {
		if (level == null) return;
		NonNullList<ItemStack> stacks = NonNullList.create();
		for (int i = 0; i < handler.getSlots(); i++) {
			stacks.add(handler.getStackInSlot(i).copy());
		}
		Containers.dropContents(level, worldPosition, stacks);
	}
}
