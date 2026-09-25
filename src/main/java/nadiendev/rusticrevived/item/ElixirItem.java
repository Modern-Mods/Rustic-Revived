package nadiendev.rusticrevived.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

/**
 * Elixir: stackable drinkable potion using the potion_contents component (legacy ItemElixir).
 * Named after its effects ("Elixir of Speed, Strength").
 */
public class ElixirItem extends Item {
	private static final int USE_DURATION = 24;

	public ElixirItem(Properties properties) {
		super(properties);
	}

	/** An elixir with the given effects. */
	public static ItemStack withEffects(Item elixir, List<MobEffectInstance> effects) {
		ItemStack stack = new ItemStack(elixir);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects));
		return stack;
	}

	public static PotionContents getContents(ItemStack stack) {
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return withEffects(this, List.of(new MobEffectInstance(MobEffects.HEAL, 1, 0)));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return ItemUtils.startUsingInstantly(level, player, hand);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return USE_DURATION;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide) {
			Player player = entity instanceof Player p ? p : null;
			getContents(stack).forEachEffect(effect -> {
				if (effect.getEffect().value().isInstantenous()) {
					effect.getEffect().value().applyInstantenousEffect(player, player, entity, effect.getAmplifier(), 1.0D);
				} else {
					entity.addEffect(new MobEffectInstance(effect));
				}
			});
		}
		return FluidBottleItem.finishDrinking(stack, entity);
	}

	@Override
	public Component getName(ItemStack stack) {
		MutableComponent name = Component.translatable("rusticrevived.elixir.prefix");
		boolean first = true;
		for (MobEffectInstance effect : getContents(stack).getAllEffects()) {
			if (!first) {
				name.append(Component.translatable("rusticrevived.elixir.separator"));
			}
			name.append(effect.getEffect().value().getDisplayName());
			first = false;
		}
		return name;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		getContents(stack).addPotionTooltip(tooltip::add, 1.0F, context.tickRate());
	}
}
