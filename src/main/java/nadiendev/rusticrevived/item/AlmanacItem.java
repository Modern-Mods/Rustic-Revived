package nadiendev.rusticrevived.item;

import nadiendev.rusticrevived.client.book.AlmanacScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The Almanac, opens Rustic's guide book (legacy ItemBook). It is also the Eris banner pattern
 * ingredient and is given back by that recipe.
 */
public class AlmanacItem extends Item {
	public AlmanacItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide) {
			// only reached on the physical client, the screen class is never loaded on a dedicated server
			AlmanacScreen.open();
		}
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack stack) {
		return stack.copyWithCount(1);
	}
}
