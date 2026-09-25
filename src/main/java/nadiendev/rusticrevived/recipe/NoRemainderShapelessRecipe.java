package nadiendev.rusticrevived.recipe;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

/**
 * Shapeless recipe that consumes containers instead of returning them (legacy
 * RecipeNonIngredientReturn): ale wort consumes the water bucket, emptying a fluid bottle or a
 * liquid barrel just resets it. Same JSON format as {@code minecraft:crafting_shapeless}.
 */
public class NoRemainderShapelessRecipe extends NormalCraftingRecipe {

	public static final MapCodec<NoRemainderShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
			CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
			Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(o -> o.ingredients)
	).apply(i, NoRemainderShapelessRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, NoRemainderShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
			CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
			ItemStackTemplate.STREAM_CODEC, o -> o.result,
			Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.ingredients,
			NoRemainderShapelessRecipe::new);

	private final ItemStackTemplate result;
	private final List<Ingredient> ingredients;
	/** Vanilla shapeless logic (matching, assembling, recipe book display). */
	private final ShapelessRecipe shapeless;

	public NoRemainderShapelessRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result,
			List<Ingredient> ingredients) {
		super(commonInfo, bookInfo);
		this.result = result;
		this.ingredients = ingredients;
		this.shapeless = new ShapelessRecipe(commonInfo, bookInfo, result, ingredients);
	}

	public ItemStackTemplate result() {
		return result;
	}

	public List<Ingredient> ingredients() {
		return ingredients;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return shapeless.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return shapeless.assemble(input);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		return NonNullList.withSize(input.size(), ItemStack.EMPTY);
	}

	@Override
	protected PlacementInfo createPlacementInfo() {
		return PlacementInfo.create(ingredients);
	}

	@Override
	public List<RecipeDisplay> display() {
		return shapeless.display();
	}

	@Override
	public RecipeSerializer<NoRemainderShapelessRecipe> getSerializer() {
		return ModRecipes.NO_REMAINDER_SHAPELESS_SERIALIZER.get();
	}
}
