package nadiendev.rusticrevived.compat.jei;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.client.alchemy.AdvancedCondenserScreen;
import nadiendev.rusticrevived.client.alchemy.BrewingBarrelScreen;
import nadiendev.rusticrevived.client.alchemy.CondenserScreen;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * JEI integration (legacy RusticJEIPlugin): categories for the crushing tub, drying basin, both
 * alchemic condensers and the brewing barrel, crafting displays for olive oiling, vanta oiling and
 * cabinets, and subtypes for fluid bottles, liquid barrels and elixirs.
 */
@JeiPlugin
public class RusticJeiPlugin implements IModPlugin {
	private static final ResourceLocation UID = RusticRevived.id("jei");

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(ModItems.FLUID_BOTTLE.get(), FluidContentSubtypeInterpreter.INSTANCE);
		registration.registerSubtypeInterpreter(ModBlocks.LIQUID_BARREL.asItem(), FluidContentSubtypeInterpreter.INSTANCE);
		registration.registerSubtypeInterpreter(ModItems.ELIXIR.get(), PotionContentsSubtypeInterpreter.INSTANCE);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(
				new CrushingTubCategory(guiHelper),
				new EvaporatingCategory(guiHelper),
				new AlchemyCategory(guiHelper, false),
				new AlchemyCategory(guiHelper, true),
				new BrewingCategory(guiHelper));
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		registration.getCraftingCategory().addExtension(CraftingDisplayRecipe.class, new CraftingDisplayExtension());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;
		RecipeManager recipes = level.getRecipeManager();
		registration.addRecipes(JeiRecipeTypes.CRUSHING_TUB, recipes.getAllRecipesFor(ModRecipes.CRUSHING_TUB.get()));
		registration.addRecipes(JeiRecipeTypes.EVAPORATING, recipes.getAllRecipesFor(ModRecipes.EVAPORATING_BASIN.get()));
		List<RecipeHolder<CondenserRecipe>> condenser = recipes.getAllRecipesFor(ModRecipes.CONDENSER.get());
		registration.addRecipes(JeiRecipeTypes.SIMPLE_ALCHEMY, condenser.stream().filter(holder -> !holder.value().advanced()).toList());
		registration.addRecipes(JeiRecipeTypes.ADVANCED_ALCHEMY, condenser.stream().filter(holder -> holder.value().advanced()).toList());
		registration.addRecipes(JeiRecipeTypes.BREWING, recipes.getAllRecipesFor(ModRecipes.BREWING.get()));
		registration.addRecipes(RecipeTypes.CRAFTING, CraftingDisplays.create(condenser));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addRecipeClickArea(CondenserScreen.class, 44, 29, 50, 28, JeiRecipeTypes.SIMPLE_ALCHEMY);
		registration.addRecipeClickArea(AdvancedCondenserScreen.class, 44, 17, 50, 53, JeiRecipeTypes.SIMPLE_ALCHEMY, JeiRecipeTypes.ADVANCED_ALCHEMY);
		registration.addRecipeClickArea(BrewingBarrelScreen.class, 85, 35, 24, 16, JeiRecipeTypes.BREWING);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(ModBlocks.CRUSHING_TUB, JeiRecipeTypes.CRUSHING_TUB);
		registration.addRecipeCatalyst(ModBlocks.EVAPORATING_BASIN, JeiRecipeTypes.EVAPORATING);
		registration.addRecipeCatalyst(ModBlocks.CONDENSER, JeiRecipeTypes.SIMPLE_ALCHEMY, RecipeTypes.FUELING);
		registration.addRecipeCatalyst(ModBlocks.RETORT, JeiRecipeTypes.SIMPLE_ALCHEMY);
		registration.addRecipeCatalyst(ModBlocks.CONDENSER_ADVANCED, JeiRecipeTypes.ADVANCED_ALCHEMY, JeiRecipeTypes.SIMPLE_ALCHEMY, RecipeTypes.FUELING);
		registration.addRecipeCatalyst(ModBlocks.RETORT_ADVANCED, JeiRecipeTypes.ADVANCED_ALCHEMY, JeiRecipeTypes.SIMPLE_ALCHEMY);
		registration.addRecipeCatalyst(ModBlocks.BREWING_BARREL, JeiRecipeTypes.BREWING);
	}
}
