package nadiendev.rusticrevived.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * Shapeless recipe that consumes containers instead of returning them (legacy
 * RecipeNonIngredientReturn): ale wort consumes the water bucket, emptying a fluid bottle or a
 * liquid barrel just resets it.
 */
public class NoRemainderShapelessRecipe extends ShapelessRecipe {

	public NoRemainderShapelessRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
		super(group, category, result, ingredients);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		return NonNullList.withSize(input.size(), ItemStack.EMPTY);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.NO_REMAINDER_SHAPELESS_SERIALIZER.get();
	}

	public static class Serializer implements RecipeSerializer<NoRemainderShapelessRecipe> {
		private static final MapCodec<NoRemainderShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(NoRemainderShapelessRecipe::getGroup),
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(NoRemainderShapelessRecipe::category),
				ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.getResultItem(null)),
				Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(list -> {
					if (list.isEmpty()) return DataResult.error(() -> "No ingredients for shapeless recipe");
					if (list.size() > 9) return DataResult.error(() -> "Too many ingredients for shapeless recipe");
					return DataResult.success(NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)));
				}, DataResult::success).forGetter(NoRemainderShapelessRecipe::getIngredients)
		).apply(i, NoRemainderShapelessRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, NoRemainderShapelessRecipe> STREAM_CODEC = StreamCodec.of(
				(buf, r) -> {
					buf.writeUtf(r.getGroup());
					buf.writeEnum(r.category());
					ByteBufCodecs.VAR_INT.encode(buf, r.getIngredients().size());
					for (Ingredient ingredient : r.getIngredients()) {
						Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
					}
					ItemStack.STREAM_CODEC.encode(buf, r.getResultItem(null));
				},
				buf -> {
					String group = buf.readUtf();
					CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
					int size = ByteBufCodecs.VAR_INT.decode(buf);
					NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
					ingredients.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
					ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
					return new NoRemainderShapelessRecipe(group, category, result, ingredients);
				});

		@Override
		public MapCodec<NoRemainderShapelessRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, NoRemainderShapelessRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
