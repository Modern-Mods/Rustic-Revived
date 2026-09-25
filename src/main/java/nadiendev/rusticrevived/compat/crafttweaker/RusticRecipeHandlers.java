package nadiendev.rusticrevived.compat.crafttweaker;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.natives.ingredient.ExpandFluidIngredient;
import com.blamejared.crafttweaker.natives.ingredient.ExpandSizedFluidIngredient;

import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

/**
 * CraftTweaker recipe handlers of Rustic's recipe types: {@code /ct recipes} dumps them as the
 * matching {@code addRecipe} calls and {@code /ct conflicts} detects overlapping inputs (condenser
 * recipes are never reported: their shapeless multi-slot matching has no cheap overlap test).
 * Decomposition (recipe replacing) is not supported.
 */
public final class RusticRecipeHandlers {
	private RusticRecipeHandlers() {
	}

	private static boolean overlaps(Ingredient first, Ingredient second) {
		return Arrays.stream(first.getItems()).anyMatch(second);
	}

	private static boolean overlaps(FluidIngredient first, FluidIngredient second) {
		return Arrays.stream(first.getStacks()).anyMatch(second);
	}

	private abstract static class Base<T extends Recipe<?>> implements IRecipeHandler<T> {
		@Override
		public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super T> manager, RegistryAccess registryAccess, T recipe) {
			return Optional.empty();
		}

		@Override
		public Optional<T> recompose(IRecipeManager<? super T> manager, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
			return Optional.empty();
		}
	}

	@IRecipeHandler.For(CrushingTubRecipe.class)
	public static final class CrushingTub extends Base<CrushingTubRecipe> {
		@Override
		public String dumpToCommandString(IRecipeManager<? super CrushingTubRecipe> manager, RegistryAccess registryAccess,
				RecipeHolder<CrushingTubRecipe> holder) {
			CrushingTubRecipe recipe = holder.value();
			String byproduct = recipe.byproduct().isEmpty() ? "" : ", " + CrTHelper.command(recipe.byproduct());
			return String.format("%s.addRecipe(%s, %s, %s%s);", manager.getCommandString(), CrTHelper.quote(holder),
					CrTHelper.command(recipe.result()), CrTHelper.command(recipe.ingredient()), byproduct);
		}

		@Override
		public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super CrushingTubRecipe> manager, CrushingTubRecipe first, U second) {
			return second instanceof CrushingTubRecipe other && overlaps(first.ingredient(), other.ingredient());
		}
	}

	@IRecipeHandler.For(EvaporatingBasinRecipe.class)
	public static final class EvaporatingBasin extends Base<EvaporatingBasinRecipe> {
		@Override
		public String dumpToCommandString(IRecipeManager<? super EvaporatingBasinRecipe> manager, RegistryAccess registryAccess,
				RecipeHolder<EvaporatingBasinRecipe> holder) {
			EvaporatingBasinRecipe recipe = holder.value();
			return String.format("%s.addRecipe(%s, %s, %s, %d);", manager.getCommandString(), CrTHelper.quote(holder),
					CrTHelper.command(recipe.result()), ExpandSizedFluidIngredient.asCTFluidIngredient(recipe.fluid()).getCommandString(),
					recipe.time());
		}

		@Override
		public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super EvaporatingBasinRecipe> manager, EvaporatingBasinRecipe first,
				U second) {
			return second instanceof EvaporatingBasinRecipe other && overlaps(first.fluid().ingredient(), other.fluid().ingredient());
		}
	}

	@IRecipeHandler.For(CondenserRecipe.class)
	public static final class Condenser extends Base<CondenserRecipe> {
		@Override
		public String dumpToCommandString(IRecipeManager<? super CondenserRecipe> manager, RegistryAccess registryAccess,
				RecipeHolder<CondenserRecipe> holder) {
			CondenserRecipe recipe = holder.value();
			String inputs = recipe.ingredients().stream().map(CrTHelper::command).collect(Collectors.joining(", ", "[", "]"));
			return String.format("%s.addRecipe(%s, %s, %s, %s, %s, %s, %d, %s);", manager.getCommandString(), CrTHelper.quote(holder),
					CrTHelper.command(recipe.result()), inputs, recipe.modifier().map(CrTHelper::command).orElse("null"),
					recipe.bottle().isEmpty() ? "<item:minecraft:air>" : CrTHelper.command(recipe.bottle()),
					ExpandSizedFluidIngredient.asCTFluidIngredient(recipe.fluid()).getCommandString(), recipe.time(), recipe.advanced());
		}

		@Override
		public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super CondenserRecipe> manager, CondenserRecipe first, U second) {
			return false;
		}
	}

	@IRecipeHandler.For(BrewingRecipe.class)
	public static final class Brewing extends Base<BrewingRecipe> {
		@Override
		public String dumpToCommandString(IRecipeManager<? super BrewingRecipe> manager, RegistryAccess registryAccess,
				RecipeHolder<BrewingRecipe> holder) {
			BrewingRecipe recipe = holder.value();
			return String.format("%s.addRecipe(%s, %s, %s, \"%s\");", manager.getCommandString(), CrTHelper.quote(holder),
					CrTHelper.command(recipe.result()), ExpandFluidIngredient.asCTFluidIngredient(recipe.input()).getCommandString(),
					recipe.quality().getSerializedName());
		}

		@Override
		public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super BrewingRecipe> manager, BrewingRecipe first, U second) {
			return second instanceof BrewingRecipe other && overlaps(first.input(), other.input());
		}
	}
}
