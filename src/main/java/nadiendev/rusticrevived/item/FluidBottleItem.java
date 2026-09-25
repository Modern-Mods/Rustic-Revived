package nadiendev.rusticrevived.item;

import java.util.function.Consumer;

import nadiendev.rusticrevived.fluid.DrinkableFluidType;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Bottle holding {@value #CAPACITY} mB of any drinkable fluid in the fluid component (legacy
 * ItemFluidBottle). Drinking it (a default drink consumable) applies the fluid's effect and gives
 * the glass bottle back; the glass bottle is also its crafting remainder.
 */
public class FluidBottleItem extends Item {
	public static final int CAPACITY = 250;

	public FluidBottleItem(Properties properties) {
		super(properties.component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK).usingConvertsTo(Items.GLASS_BOTTLE)
				.craftRemainder(Items.GLASS_BOTTLE));
	}

	/** The fluid in the bottle ({@link FluidStack#EMPTY} for an empty one). */
	public static FluidStack getFluid(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.FLUID.get(), SimpleFluidContent.EMPTY).copy();
	}

	/** Whether bottles can hold the fluid (legacy VALID_FLUIDS: every drinkable fluid). */
	public static boolean canHold(FluidStack fluid) {
		return fluid.getFluidType() instanceof DrinkableFluidType;
	}

	/** A full bottle of the fluid, components (booze quality...) included. */
	public static ItemStack filled(FluidStack fluid) {
		ItemStack stack = new ItemStack(ModItems.FLUID_BOTTLE.get());
		stack.set(ModDataComponents.FLUID.get(), SimpleFluidContent.copyOf(fluid.copyWithAmount(CAPACITY)));
		return stack;
	}

	@Override
	public ItemStack getDefaultInstance() {
		return filled(new FluidStack(ModFluids.OLIVE_OIL.get(), CAPACITY));
	}

	/** Only drinkable fluids can be drunk. */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (canHold(getFluid(player.getItemInHand(hand)))) {
			return super.use(level, player, hand);
		}
		return InteractionResult.PASS;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		FluidStack fluid = getFluid(stack);
		if (!level.isClientSide() && entity instanceof Player player && fluid.getFluidType() instanceof DrinkableFluidType drinkable) {
			drinkable.onDrank(level, player, stack, fluid);
		}
		return super.finishUsingItem(stack, level, entity);
	}

	@Override
	public Component getName(ItemStack stack) {
		FluidStack fluid = getFluid(stack);
		if (fluid.isEmpty()) {
			return Items.GLASS_BOTTLE.getName(new ItemStack(Items.GLASS_BOTTLE));
		}
		MutableComponent name = Component.translatable(getDescriptionId(), fluid.getHoverName());
		Rarity rarity = fluid.getFluidType().getRarity(fluid);
		return rarity == Rarity.COMMON ? name : name.withStyle(rarity.getStyleModifier());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		if (canHold(getFluid(stack))) {
			tooltip.accept(Component.translatable("tooltip.rusticrevived.drinkable").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		}
	}

	/**
	 * Item fluid handler: a bottle is filled or emptied all at once and only holds drinkable
	 * fluids; emptied bottles turn back into glass bottles.
	 */
	public static class FluidHandler extends ItemAccessFluidHandler {
		public FluidHandler(ItemAccess itemAccess) {
			super(itemAccess, ModDataComponents.FLUID.get(), CAPACITY);
		}

		@Override
		public boolean isValid(int index, FluidResource resource) {
			return super.isValid(index, resource) && canHold(resource.toStack(CAPACITY));
		}

		@Override
		public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
			int bottles = itemAccess.getAmount();
			if (amount < CAPACITY * bottles || getAmountAsLong(index) > 0) {
				return 0;
			}
			return super.insert(index, resource, CAPACITY * bottles, transaction);
		}

		@Override
		public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
			int bottles = itemAccess.getAmount();
			return amount < CAPACITY * bottles ? 0 : super.extract(index, resource, CAPACITY * bottles, transaction);
		}

		@Override
		protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
			return newAmount <= 0 ? ItemResource.of(Items.GLASS_BOTTLE) : super.update(accessResource, index, newResource, newAmount);
		}
	}
}
