package nadiendev.rusticrevived.compat.jei;

import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client copy of the machine recipes sent by the server (since 1.21.2 the client has no full
 * recipe manager; {@code CompatSetup} asks the server to sync Rustic's recipe types). JEI starts
 * its plugins from its own lowest priority {@link RecipesReceivedEvent} listener, so the map is
 * always up to date when {@link RusticJeiPlugin#registerRecipes} runs.
 */
public final class SyncedRecipes {
	private static RecipeMap recipes = RecipeMap.EMPTY;

	private SyncedRecipes() {
	}

	/** Client only, when JEI is installed. */
	public static void init() {
		NeoForge.EVENT_BUS.addListener(SyncedRecipes::onRecipesReceived);
		NeoForge.EVENT_BUS.addListener(SyncedRecipes::onLoggingOut);
	}

	public static RecipeMap get() {
		return recipes;
	}

	private static void onRecipesReceived(RecipesReceivedEvent event) {
		recipes = event.getRecipeMap();
	}

	private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		recipes = RecipeMap.EMPTY;
	}
}
