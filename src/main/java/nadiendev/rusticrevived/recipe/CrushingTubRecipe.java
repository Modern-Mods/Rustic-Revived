package nadiendev.rusticrevived.recipe;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

/**
 * Crushing tub: jumping on an item squeezes a fluid (and optionally a byproduct) out of it.
 *
 * <pre>
 * {
 *   "type": "rusticrevived:crushing_tub",
 *   "ingredient": "rusticrevived:olives",
 *   "result": {"id": "rusticrevived:olive_oil", "amount": 250},
 *   "byproduct": {"id": "rusticrevived:apple_seeds", "count": 1}   // optional
 * }
 * </pre>
 */
public record CrushingTubRecipe(Ingredient ingredient, FluidStackTemplate result, Optional<ItemStackTemplate> byproduct)
		implements RusticMachineRecipe<SingleRecipeInput> {

	public static final MapCodec<CrushingTubRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Ingredient.CODEC.fieldOf("ingredient").forGetter(CrushingTubRecipe::ingredient),
			FluidStackTemplate.CODEC.fieldOf("result").forGetter(CrushingTubRecipe::result),
			ItemStackTemplate.CODEC.optionalFieldOf("byproduct").forGetter(CrushingTubRecipe::byproduct)
	).apply(i, CrushingTubRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, CrushingTubRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, CrushingTubRecipe::ingredient,
			FluidStackTemplate.STREAM_CODEC, CrushingTubRecipe::result,
			ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC), CrushingTubRecipe::byproduct,
			CrushingTubRecipe::new);

	@Override
	public boolean matches(SingleRecipeInput input, Level level) {
		return ingredient.test(input.item());
	}

	/** Returns the byproduct (possibly empty). */
	@Override
	public ItemStack assemble(SingleRecipeInput input) {
		return getByproduct();
	}

	public FluidStack getResultFluid() {
		return result.create();
	}

	public ItemStack getByproduct() {
		return byproduct.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY);
	}

	@Override
	public RecipeSerializer<CrushingTubRecipe> getSerializer() {
		return ModRecipes.CRUSHING_TUB_SERIALIZER.get();
	}

	@Override
	public RecipeType<CrushingTubRecipe> getType() {
		return ModRecipes.CRUSHING_TUB.get();
	}
}
