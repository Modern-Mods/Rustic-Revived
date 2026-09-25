package nadiendev.rusticrevived.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Crushing tub: jumping on an item squeezes a fluid (and optionally a byproduct) out of it.
 *
 * <pre>
 * {
 *   "type": "rusticrevived:crushing_tub",
 *   "ingredient": {"item": "rusticrevived:olives"},
 *   "result": {"id": "rusticrevived:olive_oil", "amount": 250},
 *   "byproduct": {"id": "rusticrevived:apple_seeds", "count": 1}   // optional
 * }
 * </pre>
 */
public record CrushingTubRecipe(Ingredient ingredient, FluidStack result, ItemStack byproduct) implements Recipe<SingleRecipeInput> {

	public static final MapCodec<CrushingTubRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(CrushingTubRecipe::ingredient),
			FluidStack.CODEC.fieldOf("result").forGetter(CrushingTubRecipe::result),
			ItemStack.OPTIONAL_CODEC.optionalFieldOf("byproduct", ItemStack.EMPTY).forGetter(CrushingTubRecipe::byproduct)
	).apply(i, CrushingTubRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CrushingTubRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, CrushingTubRecipe::ingredient,
			FluidStack.STREAM_CODEC, CrushingTubRecipe::result,
			ItemStack.OPTIONAL_STREAM_CODEC, CrushingTubRecipe::byproduct,
			CrushingTubRecipe::new);

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return ingredient.test(input.item());
	}

	@Override
	public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
		return byproduct.copy();
	}

	public FluidStack getResultFluid() {
		return result.copy();
	}

	public ItemStack getByproduct() {
		return byproduct.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return byproduct;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.CRUSHING_TUB_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipes.CRUSHING_TUB.get();
	}
}
