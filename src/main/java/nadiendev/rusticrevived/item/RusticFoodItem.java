package nadiendev.rusticrevived.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Food with an optional custom eating time, an extra server side effect when eaten and an
 * optional flavour tooltip (legacy ItemFoodBase).
 */
public class RusticFoodItem extends Item {

	@FunctionalInterface
	public interface OnEaten {
		void onEaten(ItemStack stack, Level level, Player player);
	}

	private final int useDuration;
	@Nullable
	private final OnEaten onEaten;
	@Nullable
	private final String tooltipKey;

	public RusticFoodItem(Properties properties, int useDuration, @Nullable OnEaten onEaten, @Nullable String tooltipKey) {
		super(properties);
		this.useDuration = useDuration;
		this.onEaten = onEaten;
		this.tooltipKey = tooltipKey;
	}

	public RusticFoodItem(Properties properties) {
		this(properties, -1, null, null);
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return useDuration > 0 ? useDuration : super.getUseDuration(stack, entity);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (onEaten != null && !level.isClientSide && entity instanceof Player player) {
			onEaten.onEaten(stack, level, player);
		}
		return super.finishUsingItem(stack, level, entity);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		if (tooltipKey != null) {
			tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
		}
	}
}
