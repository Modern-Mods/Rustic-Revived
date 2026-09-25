package nadiendev.rusticrevived.item;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Food with an extra server side effect when eaten and an optional flavour tooltip (legacy
 * ItemFoodBase). Eating time and chance based effects come from the item's consumable component.
 */
public class RusticFoodItem extends Item {

	@FunctionalInterface
	public interface OnEaten {
		void onEaten(ItemStack stack, Level level, Player player);
	}

	private final @Nullable OnEaten onEaten;
	private final @Nullable String tooltipKey;

	public RusticFoodItem(Properties properties, @Nullable OnEaten onEaten, @Nullable String tooltipKey) {
		super(properties);
		this.onEaten = onEaten;
		this.tooltipKey = tooltipKey;
	}

	public RusticFoodItem(Properties properties) {
		this(properties, null, null);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (onEaten != null && !level.isClientSide() && entity instanceof Player player) {
			onEaten.onEaten(stack, level, player);
		}
		return super.finishUsingItem(stack, level, entity);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		if (tooltipKey != null) {
			tooltip.accept(Component.translatable(tooltipKey).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
		}
	}
}
