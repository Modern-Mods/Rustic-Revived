package nadiendev.rusticrevived.item;

import java.util.List;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.fluid.DrinkableFluidType;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

/**
 * Bottle holding {@value #CAPACITY} mB of any drinkable fluid in the fluid component (legacy
 * ItemFluidBottle). Drinking it applies the fluid's effect and gives the glass bottle back.
 */
public class FluidBottleItem extends Item {
	public static final int CAPACITY = 250;
	/** Name of the {@link #boozeModel} item property. */
	public static final ResourceLocation BOOZE_MODEL_PROPERTY = RusticRevived.id("booze");
	private static final int USE_DURATION = 32;

	public FluidBottleItem(Properties properties) {
		super(properties);
	}

	/** The fluid in the bottle ({@link FluidStack#EMPTY} for an empty one). */
	public static FluidStack getFluid(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.FLUID, SimpleFluidContent.EMPTY).copy();
	}

	/** Whether bottles can hold the fluid (legacy VALID_FLUIDS: every drinkable fluid). */
	public static boolean canHold(FluidStack fluid) {
		return fluid.getFluidType() instanceof DrinkableFluidType;
	}

	/** A full bottle of the fluid, components (booze quality...) included. */
	public static ItemStack filled(FluidStack fluid) {
		ItemStack stack = new ItemStack(ModItems.FLUID_BOTTLE.get());
		stack.set(ModDataComponents.FLUID, SimpleFluidContent.copyOf(fluid.copyWithAmount(CAPACITY)));
		return stack;
	}

	/**
	 * Item model property selecting the booze bottle models: 1 + index of the contained booze in
	 * {@link ModFluids#BOOZE}, 0 for any other fluid.
	 */
	public static float boozeModel(ItemStack stack) {
		FluidStack fluid = getFluid(stack);
		for (int i = 0; i < ModFluids.BOOZE.size(); i++) {
			if (ModFluids.BOOZE.get(i).get() == fluid.getFluid()) {
				return i + 1;
			}
		}
		return 0;
	}

	@Override
	public ItemStack getDefaultInstance() {
		return filled(new FluidStack(ModFluids.OLIVE_OIL.get(), CAPACITY));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (canHold(getFluid(player.getItemInHand(hand)))) {
			return ItemUtils.startUsingInstantly(level, player, hand);
		}
		return InteractionResultHolder.pass(player.getItemInHand(hand));
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
		FluidStack fluid = getFluid(stack);
		if (!level.isClientSide && entity instanceof Player player && fluid.getFluidType() instanceof DrinkableFluidType drinkable) {
			drinkable.onDrank(level, player, stack, fluid);
		}
		return finishDrinking(stack, entity);
	}

	/**
	 * Common end of drinking a bottle: stats, advancement trigger, consumes the drink and gives the
	 * glass bottle back (vanilla potion behaviour).
	 */
	public static ItemStack finishDrinking(ItemStack stack, LivingEntity entity) {
		Player player = entity instanceof Player p ? p : null;
		if (player instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
		}
		if (player != null) {
			player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
			stack.consume(1, player);
		}
		if (player == null || !player.hasInfiniteMaterials()) {
			if (stack.isEmpty()) {
				return new ItemStack(Items.GLASS_BOTTLE);
			}
			if (player != null) {
				player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
			}
		}
		entity.gameEvent(GameEvent.DRINK);
		return stack;
	}

	@Override
	public Component getName(ItemStack stack) {
		FluidStack fluid = getFluid(stack);
		if (fluid.isEmpty()) {
			return Items.GLASS_BOTTLE.getName(new ItemStack(Items.GLASS_BOTTLE));
		}
		MutableComponent name = Component.translatable(getDescriptionId(stack), fluid.getHoverName());
		Rarity rarity = fluid.getFluidType().getRarity(fluid);
		return rarity == Rarity.COMMON ? name : name.withStyle(rarity.getStyleModifier());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		if (canHold(getFluid(stack))) {
			tooltip.add(Component.translatable("tooltip.rusticrevived.drinkable").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		}
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack stack) {
		return new ItemStack(Items.GLASS_BOTTLE);
	}

	/**
	 * Item fluid handler: a bottle is filled or emptied all at once and only holds drinkable
	 * fluids; emptied bottles turn back into glass bottles.
	 */
	public static class FluidHandler extends FluidHandlerItemStack {
		public FluidHandler(ItemStack container) {
			super(ModDataComponents.FLUID, container, CAPACITY);
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			return canHold(fluid);
		}

		@Override
		public boolean isFluidValid(int tank, FluidStack stack) {
			return canHold(stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (container.getCount() != 1 || resource.getAmount() < capacity || !getFluid().isEmpty() || !canFillFluidType(resource)) {
				return 0;
			}
			if (action.execute()) {
				setFluid(resource.copyWithAmount(capacity));
			}
			return capacity;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			return resource.getAmount() < capacity ? FluidStack.EMPTY : super.drain(resource, action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			return maxDrain < capacity ? FluidStack.EMPTY : super.drain(maxDrain, action);
		}

		@Override
		protected void setContainerToEmpty() {
			super.setContainerToEmpty();
			container = new ItemStack(Items.GLASS_BOTTLE);
		}
	}
}
