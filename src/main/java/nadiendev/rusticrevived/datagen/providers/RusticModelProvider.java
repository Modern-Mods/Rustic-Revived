package nadiendev.rusticrevived.datagen.providers;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.datagen.providers.parts.AlchemyData;
import nadiendev.rusticrevived.datagen.providers.parts.DecorData;
import nadiendev.rusticrevived.datagen.providers.parts.FarmData;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import nadiendev.rusticrevived.datagen.providers.parts.StorageData;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * Block states, block models, item models and client item definitions (26.1 {@code items/}).
 * Each subsystem generates its own entries from {@code datagen.providers.parts}.
 * <p>
 * Every block and item of the {@value RusticRevived#NAMESPACE} namespace must be covered: the
 * vanilla provider validates it.
 */
public class RusticModelProvider extends ModelProvider {

	public RusticModelProvider(PackOutput output) {
		super(output, RusticRevived.NAMESPACE);
	}

	@Override
	protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		DecorData.models(this, blockModels, itemModels);
		FarmData.models(this, blockModels, itemModels);
		StorageData.models(this, blockModels, itemModels);
		AlchemyData.models(this, blockModels, itemModels);
		MiscData.models(this, blockModels, itemModels);
	}

	// ---------------------------------------------------------------- helpers shared by the parts

	public static String name(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).getPath();
	}

	/** {@code rusticrevived:block/<path>} */
	public static Identifier blockTex(String path) {
		return RusticRevived.id("block/" + path);
	}

	/** {@code rusticrevived:item/<path>} */
	public static Identifier itemTex(String path) {
		return RusticRevived.id("item/" + path);
	}

	/** A hand-made block model shipped in {@code src/main/resources}: {@code rusticrevived:block/<path>}. */
	public static Identifier existingBlockModel(String path) {
		return RusticRevived.id("block/" + path);
	}
}
