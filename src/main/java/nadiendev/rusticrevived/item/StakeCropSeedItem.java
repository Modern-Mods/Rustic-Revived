package nadiendev.rusticrevived.item;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Seeds planted on a crop stake standing on farmland or fertile soil (legacy ItemStakeCropSeed).
 */
public class StakeCropSeedItem extends Item {
	protected final Block crop;

	public StakeCropSeedItem(Block crop, Properties properties) {
		super(properties);
		this.crop = crop;
	}

	public Block getCrop() {
		return crop;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState planted = crop.defaultBlockState();
		if (!level.getBlockState(pos).is(ModBlocks.CROP_STAKE.get()) || !planted.canSurvive(level, pos)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide) {
			level.setBlock(pos, planted, Block.UPDATE_ALL);
			level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(context.getPlayer(), planted));
			level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
			context.getItemInHand().consume(1, context.getPlayer());
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
