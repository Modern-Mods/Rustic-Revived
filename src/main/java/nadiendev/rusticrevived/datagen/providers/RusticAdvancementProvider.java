package nadiendev.rusticrevived.datagen.providers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Rustic advancements (legacy assets/rustic/advancements).
 */
public class RusticAdvancementProvider extends AdvancementProvider {
	public RusticAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
		super(output, registries, helper, List.of(MiscData::advancements));
	}
}
