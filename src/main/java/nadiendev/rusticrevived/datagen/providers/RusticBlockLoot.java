package nadiendev.rusticrevived.datagen.providers;

import java.util.Set;

import nadiendev.rusticrevived.datagen.providers.parts.AlchemyData;
import nadiendev.rusticrevived.datagen.providers.parts.DecorData;
import nadiendev.rusticrevived.datagen.providers.parts.FarmData;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import nadiendev.rusticrevived.datagen.providers.parts.StorageData;
import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * Block loot tables. Every Rustic block must have one (liquid blocks excepted: they use
 * {@code noLootTable}). Each subsystem generates its own tables from
 * {@code datagen.providers.parts}; the protected vanilla helpers are re-exposed publicly here.
 */
public class RusticBlockLoot extends BlockLootSubProvider {

	public RusticBlockLoot(HolderLookup.Provider registries) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
	}

	@Override
	protected void generate() {
		DecorData.blockLoot(this);
		FarmData.blockLoot(this);
		StorageData.blockLoot(this);
		AlchemyData.blockLoot(this);
		MiscData.blockLoot(this);
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)
				.filter(b -> !(b instanceof net.minecraft.world.level.block.LiquidBlock))
				.map(b -> (Block) b)::iterator;
	}

	public HolderLookup.Provider lookup() {
		return registries;
	}

	// ---------------------------------------------------------------- public wrappers

	public void selfDrop(Block block) {
		dropSelf(block);
	}

	public void otherDrop(Block block, ItemLike drop) {
		dropOther(block, drop);
	}

	public void table(Block block, LootTable.Builder table) {
		add(block, table);
	}

	public void noDrop(Block block) {
		add(block, noDrop());
	}

	public LootTable.Builder slab(Block block) {
		return createSlabItemTable(block);
	}

	public LootTable.Builder door(Block block) {
		return createDoorTable(block);
	}

	public LootTable.Builder leaves(Block leaves, Block sapling, float... saplingChances) {
		return createLeavesDrops(leaves, sapling, saplingChances);
	}

	public LootTable.Builder silkOnly(ItemLike item) {
		return createSilkTouchOnlyTable(item);
	}

	public LootTable.Builder single(ItemLike item) {
		return createSingleItemTable(item);
	}

	public LootTable.Builder single(ItemLike item, NumberProvider count) {
		return createSingleItemTable(item, count);
	}

	public LootTable.Builder crop(Block crop, Item grown, Item seed, LootItemCondition.Builder grownCondition) {
		return createCropDrops(crop, grown, seed, grownCondition);
	}

	public LootItemCondition.Builder silkTouch() {
		return hasSilkTouch();
	}

	public LootItemCondition.Builder shears() {
		return HAS_SHEARS;
	}
}
