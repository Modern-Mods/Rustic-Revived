package nadiendev.rusticrevived.datagen.providers;

import java.util.concurrent.CompletableFuture;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.datagen.providers.parts.AlchemyData;
import nadiendev.rusticrevived.datagen.providers.parts.DecorData;
import nadiendev.rusticrevived.datagen.providers.parts.FarmData;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import nadiendev.rusticrevived.datagen.providers.parts.StorageData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

/**
 * All tag providers. The {@code tagOf} methods re-expose the protected {@code tag} builders so the
 * subsystem parts in {@code datagen.providers.parts} can fill them.
 */
public final class RusticTagProviders {
	private RusticTagProviders() {
	}

	public static class Blocks extends BlockTagsProvider {
		public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
			super(output, lookup, RusticRevived.NAMESPACE);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
			DecorData.blockTags(this);
			FarmData.blockTags(this);
			StorageData.blockTags(this);
			AlchemyData.blockTags(this);
			MiscData.blockTags(this);
		}

		public TagAppender<Block, Block> tagOf(TagKey<Block> tag) {
			return tag(tag);
		}
	}

	public static class Items extends BlockTagCopyingItemTagProvider {
		public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags) {
			super(output, lookup, blockTags, RusticRevived.NAMESPACE);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
			DecorData.itemTags(this);
			FarmData.itemTags(this);
			StorageData.itemTags(this);
			AlchemyData.itemTags(this);
			MiscData.itemTags(this);
		}

		public TagAppender<Item, Item> tagOf(TagKey<Item> tag) {
			return tag(tag);
		}

		/** Copies a block tag into the item tag of the same name. */
		public void copyTag(TagKey<Block> blockTag, TagKey<Item> itemTag) {
			copy(blockTag, itemTag);
		}
	}

	public static class Fluids extends IntrinsicHolderTagsProvider<Fluid> {
		public Fluids(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
			super(output, Registries.FLUID, lookup, f -> f.builtInRegistryHolder().key(), RusticRevived.NAMESPACE);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
			AlchemyData.fluidTags(this);
			MiscData.fluidTags(this);
		}

		public TagAppender<Fluid, Fluid> tagOf(TagKey<Fluid> tag) {
			return tag(tag);
		}
	}

	public static class EntityTypes extends IntrinsicHolderTagsProvider<EntityType<?>> {
		public EntityTypes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
			super(output, Registries.ENTITY_TYPE, lookup, e -> e.builtInRegistryHolder().key(), RusticRevived.NAMESPACE);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
			MiscData.entityTypeTags(this);
		}

		public TagAppender<EntityType<?>, EntityType<?>> tagOf(TagKey<EntityType<?>> tag) {
			return tag(tag);
		}
	}

	public static class Biomes extends KeyTagProvider<Biome> {
		public Biomes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
			super(output, Registries.BIOME, lookup, RusticRevived.NAMESPACE);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
			FarmData.biomeTags(this);
		}

		public TagAppender<ResourceKey<Biome>, Biome> tagOf(TagKey<Biome> tag) {
			return tag(tag);
		}
	}

	public static class BannerPatterns extends KeyTagProvider<BannerPattern> {
		public BannerPatterns(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
			super(output, Registries.BANNER_PATTERN, lookup, RusticRevived.NAMESPACE);
		}

		@Override
		protected void addTags(HolderLookup.Provider provider) {
			MiscData.bannerPatternTags(this);
		}

		public TagAppender<ResourceKey<BannerPattern>, BannerPattern> tagOf(TagKey<BannerPattern> tag) {
			return tag(tag);
		}
	}
}
