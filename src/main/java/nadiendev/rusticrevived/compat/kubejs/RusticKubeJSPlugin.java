package nadiendev.rusticrevived.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import nadiendev.rusticrevived.RusticRevived;

/**
 * KubeJS plugin (listed in {@code kubejs.plugins.txt}, only loaded when KubeJS is installed):
 * registers the {@link RusticRecipeSchemas recipe schemas} so
 * {@code event.recipes.rusticrevived.crushing_tub(...)} etc. work in server scripts.
 */
public class RusticKubeJSPlugin implements KubeJSPlugin {

	@Override
	public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
		registry.register(RusticRecipeSchemas.QUALITY_COMPONENT);
	}

	@Override
	public void registerRecipeFactories(RecipeFactoryRegistry registry) {
		registry.register(RusticRecipeSchemas.CONDENSER_FACTORY);
	}

	@Override
	public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
		registry.namespace(RusticRevived.NAMESPACE)
				.register("crushing_tub", RusticRecipeSchemas.CRUSHING_TUB)
				.register("evaporating_basin", RusticRecipeSchemas.EVAPORATING_BASIN)
				.register("condenser", RusticRecipeSchemas.CONDENSER)
				.register("brewing", RusticRecipeSchemas.BREWING);
	}
}
