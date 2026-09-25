package nadiendev.rusticrevived.world.loot;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import nadiendev.rusticrevived.config.RusticConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Adds grape seeds to vine drops (legacy EventHandlerCommon#onVineDropEvent): 1 in 10 by default,
 * only when {@code enableSeedDrops} is on and, with {@code grapeDropNeedsTool}, only when the vine
 * is broken with an item (or #tag) of {@code grapeToolWhitelist}.
 */
public class GrapeSeedsModifier extends LootModifier {
	public static final MapCodec<GrapeSeedsModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i).and(i.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("seed").forGetter(modifier -> modifier.seed),
			Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(modifier -> modifier.chance)
	)).apply(i, GrapeSeedsModifier::new));

	private final Item seed;
	private final float chance;

	public GrapeSeedsModifier(LootItemCondition[] conditions, int priority, Item seed, float chance) {
		super(conditions, priority);
		this.seed = seed;
		this.chance = chance;
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (RusticConfig.COMMON.enableSeedDrops.get() && hasValidTool(context) && context.getRandom().nextFloat() < chance) {
			generatedLoot.add(new ItemStack(seed));
		}
		return generatedLoot;
	}

	private static boolean hasValidTool(LootContext context) {
		if (!RusticConfig.COMMON.grapeDropNeedsTool.get()) {
			return true;
		}
		ItemInstance tool = context.getOptionalParameter(LootContextParams.TOOL);
		if (tool == null || tool.count() <= 0 || tool.is(Items.AIR)) {
			return false;
		}
		List<? extends String> whitelist = RusticConfig.COMMON.grapeToolWhitelist.get();
		for (String entry : whitelist) {
			if (entry.startsWith("#")) {
				Identifier tag = Identifier.tryParse(entry.substring(1));
				if (tag != null && tool.is(TagKey.create(Registries.ITEM, tag))) {
					return true;
				}
			} else if (BuiltInRegistries.ITEM.getKey(tool.typeHolder().value()).toString().equals(entry)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
