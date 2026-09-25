package nadiendev.rusticrevived.item;

import org.jspecify.annotations.Nullable;

import nadiendev.rusticrevived.client.book.AlmanacScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStackTemplate;
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
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			// only reached on the physical client, the screen class is never loaded on a dedicated server
			AlmanacScreen.open();
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
		return new ItemStackTemplate(instance.typeHolder());
	}
}
