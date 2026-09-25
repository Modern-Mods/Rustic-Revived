package nadiendev.rusticrevived.item;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumables;

/**
 * Elixir: stackable drinkable potion using the potion_contents component (legacy ItemElixir).
 * Named after its effects ("Elixir of Speed, Strength"). Drinking (1.2 s) applies the effects
 * through the potion contents consume listener and gives the glass bottle back; the effect
 * tooltip comes from the potion contents component.
 */
public class ElixirItem extends Item {
	private static final float DRINK_SECONDS = 1.2F;

	public ElixirItem(Properties properties) {
		super(properties.component(DataComponents.CONSUMABLE, Consumables.defaultDrink().consumeSeconds(DRINK_SECONDS).build())
				.usingConvertsTo(Items.GLASS_BOTTLE));
	}

	/** An elixir with the given effects. */
	public static ItemStack withEffects(Item elixir, List<MobEffectInstance> effects) {
		ItemStack stack = new ItemStack(elixir);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects, Optional.empty()));
		return stack;
	}

	/** Recipe result of an elixir with the given effects (usable before item components are bound, e.g. in datagen). */
	public static ItemStackTemplate template(Item elixir, List<MobEffectInstance> effects) {
		return new ItemStackTemplate(elixir, DataComponentPatch.builder()
				.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), effects, Optional.empty())).build());
	}

	public static PotionContents getContents(ItemStack stack) {
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
	}

	@Override
	public ItemStack getDefaultInstance() {
		return withEffects(this, List.of(new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 0)));
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
}
