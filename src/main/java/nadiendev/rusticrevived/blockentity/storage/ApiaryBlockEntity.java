package nadiendev.rusticrevived.blockentity.storage;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.menu.storage.ApiaryMenu;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Apiary (legacy TileEntityApiary). Bees in the bee slot reproduce and fill the honeycomb slot at a
 * rate that grows with their number, and randomly age crops up to four blocks away horizontally and
 * one block vertically. Automation: the bottom face exposes the honeycomb slot, every other face the
 * bee slot.
 */
public class ApiaryBlockEntity extends BlockEntity implements MenuProvider {
	private final ItemStackHandler bees = new FilteredHandler() {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.is(ModItems.BEE.get());
		}
	};
	private final ItemStackHandler honeycomb = new FilteredHandler() {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.is(ModItems.HONEYCOMB.get());
		}
	};
	private int reproductionTimer;
	private int productionTimer;

	public ApiaryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.APIARY.get(), pos, state);
	}

	/** Item handler capability: honeycomb from below, bees from any other side. */
	public IItemHandler getItemHandler(@Nullable Direction side) {
		return side == Direction.DOWN ? honeycomb : bees;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ApiaryBlockEntity apiary) {
		apiary.tick((ServerLevel) level);
	}

	private void tick(ServerLevel level) {
		ItemStack beeStack = bees.getStackInSlot(0);
		int numBees = beeStack.is(ModItems.BEE.get()) ? beeStack.getCount() : 0;
		if (numBees <= 0) return;

		float speed = numBees / 20F + 1F;
		int reproductionTime = (int) (RusticConfig.COMMON.beeReproductionMultiplier.get() * (1600F / speed));
		int productionTime = (int) (RusticConfig.COMMON.beeHoneycombMultiplier.get() * (800F / speed));

		if (++reproductionTimer >= reproductionTime) {
			reproductionTimer = 0;
			if (numBees < beeStack.getMaxStackSize()) {
				bees.setStackInSlot(0, beeStack.copyWithCount(numBees + 1));
			}
		}
		if (++productionTimer >= productionTime) {
			productionTimer = 0;
			ItemStack combs = honeycomb.getStackInSlot(0);
			if (combs.isEmpty()) {
				honeycomb.setStackInSlot(0, new ItemStack(ModItems.HONEYCOMB.get()));
			} else if (combs.is(ModItems.HONEYCOMB.get()) && combs.getCount() < combs.getMaxStackSize()) {
				honeycomb.setStackInSlot(0, combs.copyWithCount(combs.getCount() + 1));
			}
		}

		double growth = RusticConfig.COMMON.beeGrowthMultiplier.get();
		if (growth > 0 && level.random.nextInt(Mth.ceil(2048D / (numBees * growth))) == 0) {
			pollinate(level, level.random);
		}
		setChanged();
	}

	/** Random ticks the first growable block of a random column around the apiary. */
	private void pollinate(ServerLevel level, RandomSource random) {
		int dx = random.nextInt(9) - 4;
		int dz = random.nextInt(9) - 4;
		for (int dy = 1; dy >= -1; dy--) {
			BlockPos target = worldPosition.offset(dx, dy, dz);
			BlockState state = level.getBlockState(target);
			if (state.is(ModTags.Blocks.BEE_GROWABLES) || state.getBlock() instanceof BonemealableBlock) {
				if (state.isRandomlyTicking()) {
					state.randomTick(level, target, random);
				}
				return;
			}
		}
	}

	public void dropContents(Level level, BlockPos pos) {
		Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), bees.getStackInSlot(0));
		Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), honeycomb.getStackInSlot(0));
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.rusticrevived.apiary");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new ApiaryMenu(containerId, playerInventory, bees, honeycomb, ContainerLevelAccess.create(level, worldPosition));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		bees.deserializeNBT(registries, tag.getCompound("bees"));
		honeycomb.deserializeNBT(registries, tag.getCompound("honeycomb"));
		reproductionTimer = tag.getInt("reproduction_timer");
		productionTimer = tag.getInt("production_timer");
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("bees", bees.serializeNBT(registries));
		tag.put("honeycomb", honeycomb.serializeNBT(registries));
		tag.putInt("reproduction_timer", reproductionTimer);
		tag.putInt("production_timer", productionTimer);
	}

	/** One slot handler that only accepts its item and marks the apiary dirty. */
	private abstract class FilteredHandler extends ItemStackHandler {
		FilteredHandler() {
			super(1);
		}

		@Override
		protected void onContentsChanged(int slot) {
			setChanged();
		}
	}
}
