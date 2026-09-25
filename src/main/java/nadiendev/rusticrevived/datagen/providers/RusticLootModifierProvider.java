package nadiendev.rusticrevived.datagen.providers;

import java.util.concurrent.CompletableFuture;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.datagen.providers.parts.FarmData;
import nadiendev.rusticrevived.datagen.providers.parts.MiscData;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

/**
 * Global loot modifiers (seeds from grass, grape seeds from vines, ...).
 */
public class RusticLootModifierProvider extends GlobalLootModifierProvider {
	public RusticLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, RusticRevived.NAMESPACE);
	}

	@Override
	protected void start() {
		FarmData.lootModifiers(this);
		MiscData.lootModifiers(this);
	}

	public <T extends IGlobalLootModifier> void modifier(String name, T instance) {
		add(name, instance);
	}
}
