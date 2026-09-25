package nadiendev.rusticrevived.blockentity.storage;

import org.jspecify.annotations.Nullable;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.menu.storage.ApiaryMenu;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * Apiary (legacy TileEntityApiary). Bees in the bee slot reproduce and fill the honeycomb slot at a
 * rate that grows with their number, and randomly age crops up to four blocks away horizontally and
 * one block vertically. Automation: the bottom face exposes the honeycomb slot, every other face the
 * bee slot.
 */
public class ApiaryBlockEntity extends BlockEntity implements MenuProvider {
	private final SlotHandler bees = new SlotHandler(ModItems.BEE.get());
	private final SlotHandler honeycomb = new SlotHandler(ModItems.HONEYCOMB.get());
	private int reproductionTimer;
	private int productionTimer;

	public ApiaryBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.APIARY.get(), pos, state);
	}

	/** Item handler capability: honeycomb from below, bees from any other side. */
	public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
		return side == Direction.DOWN ? honeycomb : bees;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ApiaryBlockEntity apiary) {
		apiary.tick((ServerLevel) level);
	}

	private void tick(ServerLevel level) {
		ItemStack beeStack = bees.getStack();
		int numBees = beeStack.is(ModItems.BEE.get()) ? beeStack.getCount() : 0;
		if (numBees <= 0) return;

		float speed = numBees / 20F + 1F;
		int reproductionTime = (int) (RusticConfig.COMMON.beeReproductionMultiplier.get() * (1600F / speed));
		int productionTime = (int) (RusticConfig.COMMON.beeHoneycombMultiplier.get() * (800F / speed));

		if (++reproductionTimer >= reproductionTime) {
			reproductionTimer = 0;
			if (numBees < beeStack.getMaxStackSize()) {
				bees.setStack(beeStack.copyWithCount(numBees + 1));
			}
		}
		if (++productionTimer >= productionTime) {
			productionTimer = 0;
			ItemStack combs = honeycomb.getStack();
			if (combs.isEmpty()) {
				honeycomb.setStack(new ItemStack(ModItems.HONEYCOMB.get()));
			} else if (combs.is(ModItems.HONEYCOMB.get()) && combs.getCount() < combs.getMaxStackSize()) {
				honeycomb.setStack(combs.copyWithCount(combs.getCount() + 1));
			}
		}

		double growth = RusticConfig.COMMON.beeGrowthMultiplier.get();
		if (growth > 0 && level.getRandom().nextInt(Mth.ceil(2048D / (numBees * growth))) == 0) {
			pollinate(level, level.getRandom());
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

	/** Drops the bees and the honeycomb when the apiary is removed. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), bees.getStack());
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), honeycomb.getStack());
		}
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
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		bees.deserialize(input.childOrEmpty("bees"));
		honeycomb.deserialize(input.childOrEmpty("honeycomb"));
		reproductionTimer = input.getIntOr("reproduction_timer", 0);
		productionTimer = input.getIntOr("production_timer", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		bees.serialize(output.child("bees"));
		honeycomb.serialize(output.child("honeycomb"));
		output.putInt("reproduction_timer", reproductionTimer);
		output.putInt("production_timer", productionTimer);
	}

	/** One slot handler that only accepts its item and marks the apiary dirty. */
	private final class SlotHandler extends ItemStacksResourceHandler {
		private final Item accepted;

		SlotHandler(Item accepted) {
			super(1);
			this.accepted = accepted;
		}

		ItemStack getStack() {
			return getResource(0).toStack(getAmountAsInt(0));
		}

		void setStack(ItemStack stack) {
			set(0, ItemResource.of(stack), stack.getCount());
		}

		@Override
		public boolean isValid(int index, ItemResource resource) {
			return resource.is(accepted);
		}

		@Override
		protected void onContentsChanged(int index, ItemStack previousContents) {
			setChanged();
		}
	}
}
