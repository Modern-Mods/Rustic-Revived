package nadiendev.rusticrevived.recipe;

import java.util.List;

import javax.annotation.Nullable;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.item.ElixirItem;
import nadiendev.rusticrevived.item.FluidBottleItem;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModRecipes;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Vanta oiling (legacy RecipeVantaOil): weapon + vanta oil bottle + any number of potions or
 * elixirs with the same single effect -> the weapon coated with that effect. The bottles come
 * back empty.
 * <p>
 * The {@link ModDataComponents.VantaOil} component holds one effect whose duration is the total
 * duration left on the weapon (for instant effects: the hits left); every hit applies up to
 * {@value #HIT_DURATION} ticks of it.
 */
public class VantaOilRecipe extends CustomRecipe {
	/** Duration applied by one hit. */
	public static final int HIT_DURATION = 15 * 20;
	/** A leftover shorter than this is applied with the last full hit. */
	public static final int MAX_HIT_DURATION = HIT_DURATION + 5 * 20;

	public VantaOilRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return !craft(input).isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		return craft(input);
	}

	private static ItemStack craft(CraftingInput input) {
		ItemStack weapon = ItemStack.EMPTY;
		boolean oil = false;
		Holder<MobEffect> effect = null;
		int amplifier = 0;
		int duration = 0;
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.isEmpty()) continue;
			if (isVantaOilBottle(stack)) {
				if (oil) return ItemStack.EMPTY;
				oil = true;
				continue;
			}
			MobEffectInstance ingredient = getIngredientEffect(stack);
			if (ingredient != null) {
				if (effect == null) {
					effect = ingredient.getEffect();
					amplifier = ingredient.getAmplifier();
				} else if (!effect.equals(ingredient.getEffect())) {
					return ItemStack.EMPTY;
				} else {
					amplifier = Math.min(amplifier, ingredient.getAmplifier());
				}
				duration += effect.value().isInstantenous() ? 1 : Math.max(ingredient.getDuration(), 1);
				continue;
			}
			if (isOilable(stack) && weapon.isEmpty()) {
				weapon = stack;
				continue;
			}
			return ItemStack.EMPTY;
		}
		if (weapon.isEmpty() || !oil || effect == null) {
			return ItemStack.EMPTY;
		}
		MobEffectInstance current = getOil(weapon);
		if (current != null) {
			if (!current.getEffect().equals(effect)) return ItemStack.EMPTY;
			duration += current.getDuration();
			amplifier = Math.min(amplifier, current.getAmplifier());
		}
		ItemStack result = weapon.copyWithCount(1);
		result.set(ModDataComponents.VANTA_OIL, createOil(new MobEffectInstance(effect, duration, amplifier)));
		return result;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (isVantaOilBottle(stack) || getIngredientEffect(stack) != null) {
				remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
			}
		}
		return remaining;
	}

	// ---------------------------------------------------------------- vanta oil helpers

	public static boolean isVantaOilBottle(ItemStack stack) {
		return stack.getItem() instanceof FluidBottleItem && FluidBottleItem.getFluid(stack).getFluid() == ModFluids.VANTA_OIL.get();
	}

	/** The single effect of a potion or elixir, or null. */
	@Nullable
	public static MobEffectInstance getIngredientEffect(ItemStack stack) {
		if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION) && !(stack.getItem() instanceof ElixirItem)) {
			return null;
		}
		PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		MobEffectInstance single = null;
		for (MobEffectInstance effect : contents.getAllEffects()) {
			if (single != null) return null;
			single = effect;
		}
		return single;
	}

	/** Swords, axes, the {@link ModTags.Items#VANTA_OILABLE} tag and the config whitelist ({@code id} or {@code #tag}). */
	public static boolean isOilable(ItemStack stack) {
		if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.is(ModTags.Items.VANTA_OILABLE)) {
			return true;
		}
		String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		for (String entry : RusticConfig.COMMON.vantaOilWhitelist.get()) {
			if (entry.startsWith("#")) {
				ResourceLocation tag = ResourceLocation.tryParse(entry.substring(1));
				if (tag != null && stack.is(TagKey.create(Registries.ITEM, tag))) return true;
			} else if (entry.equals(id)) {
				return true;
			}
		}
		return false;
	}

	/** The effect (with its total duration) coating the weapon, or null. */
	@Nullable
	public static MobEffectInstance getOil(ItemStack stack) {
		ModDataComponents.VantaOil oil = stack.get(ModDataComponents.VANTA_OIL);
		return oil == null || oil.effects().isEmpty() ? null : oil.effects().get(0);
	}

	/** Coating holding the effect for its whole duration. */
	public static ModDataComponents.VantaOil createOil(MobEffectInstance total) {
		return new ModDataComponents.VantaOil(List.of(total), remainingUses(total));
	}

	/** Number of hits left for the total effect duration. */
	public static int remainingUses(MobEffectInstance total) {
		int duration = total.getDuration();
		if (duration <= 0) return 0;
		if (total.getEffect().value().isInstantenous()) return duration;
		int hits = duration / HIT_DURATION;
		if (duration % HIT_DURATION > MAX_HIT_DURATION - HIT_DURATION || hits == 0) {
			hits++;
		}
		return hits;
	}

	/** Duration applied by the next hit. */
	public static int nextHitDuration(int totalDuration) {
		return totalDuration > MAX_HIT_DURATION ? HIT_DURATION : totalDuration;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 3;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.VANTA_OIL_SERIALIZER.get();
	}
}
