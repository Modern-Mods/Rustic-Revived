package nadiendev.rusticrevived.world.loot;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import nadiendev.rusticrevived.config.RusticConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Adds Rustic seeds to grass drops (legacy {@code MinecraftForge.addGrassSeed}). Like in 1.12, a
 * broken grass block rolls a seed 1 time in 8, and each Rustic seed weighs {@code seedDropRate}
 * against the weight 10 of wheat seeds.
 */
public class GrassSeedsModifier extends LootModifier {
	public static final MapCodec<GrassSeedsModifier> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i).and(
			BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("seeds").forGetter(modifier -> modifier.seeds)
	).apply(i, GrassSeedsModifier::new));
	private static final int GRASS_SEED_CHANCE = 8;
	private static final int WHEAT_SEED_WEIGHT = 10;

	private final List<Item> seeds;

	public GrassSeedsModifier(LootItemCondition[] conditions, int priority, List<Item> seeds) {
		super(conditions, priority);
		this.seeds = seeds;
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (seeds.isEmpty() || !RusticConfig.COMMON.enableSeedDrops.get()) {
			return generatedLoot;
		}
		RandomSource random = context.getRandom();
		int rustic = RusticConfig.COMMON.seedDropRate.get() * seeds.size();
		if (random.nextInt(GRASS_SEED_CHANCE) == 0 && random.nextInt(WHEAT_SEED_WEIGHT + rustic) >= WHEAT_SEED_WEIGHT) {
			generatedLoot.add(new ItemStack(seeds.get(random.nextInt(seeds.size()))));
		}
		return generatedLoot;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
