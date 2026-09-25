package nadiendev.rusticrevived.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * Alchemic condenser recipe (legacy BasicCondenserRecipe / AdvancedCondenserRecipe).
 * <p>
 * Basic recipes use at most two ingredients and no modifier; they work in both condensers.
 * Advanced recipes use up to three ingredients plus an optional modifier and only work in the
 * advanced condenser.
 *
 * <pre>
 * {
 *   "type": "rusticrevived:condenser",
 *   "advanced": true,
 *   "ingredients": ["rusticrevived:ginseng", "minecraft:bone", "minecraft:gunpowder"],
 *   "modifier": "rusticrevived:horsetail",                              // optional
 *   "bottle": "minecraft:glass_bottle",                                 // optional, default glass bottle
 *   "fluid": {"ingredient": "minecraft:water", "amount": 125},          // optional, default 125 mB water
 *   "time": 300,                                                        // optional, default 400 basic / 300 advanced
 *   "result": {"id": "rusticrevived:elixir", "components": {"minecraft:potion_contents": {...}}}
 * }
 * </pre>
 */
public record CondenserRecipe(boolean advanced, List<Ingredient> ingredients, Optional<Ingredient> modifier, Ingredient bottle,
		SizedFluidIngredient fluid, int time, ItemStackTemplate result) implements RusticMachineRecipe<CondenserRecipeInput> {

	public static final int BASIC_TIME = 400;
	public static final int ADVANCED_TIME = 300;

	public static Ingredient defaultBottle() {
		return Ingredient.of(Items.GLASS_BOTTLE);
	}

	public static SizedFluidIngredient defaultFluid() {
		return SizedFluidIngredient.of(Fluids.WATER, 125);
	}

	public static final MapCodec<CondenserRecipe> CODEC = RecordCodecBuilder.<CondenserRecipe>mapCodec(i -> i.group(
			Codec.BOOL.optionalFieldOf("advanced", false).forGetter(CondenserRecipe::advanced),
			Ingredient.CODEC.listOf(1, 3).fieldOf("ingredients").forGetter(CondenserRecipe::ingredients),
			Ingredient.CODEC.optionalFieldOf("modifier").forGetter(CondenserRecipe::modifier),
			Ingredient.CODEC.optionalFieldOf("bottle").forGetter(r -> Optional.of(r.bottle())),
			SizedFluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(r -> Optional.of(r.fluid())),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("time", 0).forGetter(CondenserRecipe::time),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(CondenserRecipe::result)
	).apply(i, (adv, ings, mod, bottle, fluid, time, result) -> new CondenserRecipe(adv, ings, mod,
			bottle.orElseGet(CondenserRecipe::defaultBottle), fluid.orElseGet(CondenserRecipe::defaultFluid), time, result)))
			.validate(CondenserRecipe::validate);

	public static final StreamCodec<RegistryFriendlyByteBuf, CondenserRecipe> STREAM_CODEC = StreamCodec.of(CondenserRecipe::encode, CondenserRecipe::decode);

	private static void encode(RegistryFriendlyByteBuf buf, CondenserRecipe r) {
		buf.writeBoolean(r.advanced);
		Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, r.ingredients);
		Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, r.modifier);
		Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.bottle);
		SizedFluidIngredient.STREAM_CODEC.encode(buf, r.fluid);
		buf.writeVarInt(r.time);
		ItemStackTemplate.STREAM_CODEC.encode(buf, r.result);
	}

	private static CondenserRecipe decode(RegistryFriendlyByteBuf buf) {
		return new CondenserRecipe(buf.readBoolean(),
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
				Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf),
				Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
				SizedFluidIngredient.STREAM_CODEC.decode(buf),
				buf.readVarInt(),
				ItemStackTemplate.STREAM_CODEC.decode(buf));
	}

	private static DataResult<CondenserRecipe> validate(CondenserRecipe recipe) {
		if (!recipe.advanced && recipe.modifier.isPresent()) {
			return DataResult.error(() -> "Basic condenser recipes can not have a modifier");
		}
		if (!recipe.advanced && recipe.ingredients.size() > 2) {
			return DataResult.error(() -> "Basic condenser recipes can have at most 2 ingredients");
		}
		return DataResult.success(recipe);
	}

	/** Brewing time in ticks. */
	public int getTime() {
		return time > 0 ? time : (advanced ? ADVANCED_TIME : BASIC_TIME);
	}

	@Override
	public boolean matches(CondenserRecipeInput input, Level level) {
		if (advanced && !input.advancedDevice()) return false;
		if (!bottle.test(input.bottle())) return false;
		if (!fluid.ingredient().test(input.fluid())) return false;
		if (modifier.isPresent()) {
			if (!modifier.get().test(input.modifier())) return false;
		} else if (!input.modifier().isEmpty()) {
			return false;
		}
		return matchIngredients(input.ingredients()) != null;
	}

	/**
	 * Shapeless matching of the ingredient slots.
	 *
	 * @return for every slot, the index of the recipe ingredient it satisfies (-1 for empty slots),
	 * or null if the slots do not match this recipe
	 */
	public int[] matchIngredients(List<ItemStack> slots) {
		int[] assignment = new int[slots.size()];
		boolean[] used = new boolean[ingredients.size()];
		int matched = 0;
		for (int s = 0; s < slots.size(); s++) {
			ItemStack stack = slots.get(s);
			assignment[s] = -1;
			if (stack.isEmpty()) continue;
			boolean found = false;
			for (int r = 0; r < ingredients.size(); r++) {
				if (!used[r] && ingredients.get(r).test(stack)) {
					used[r] = true;
					assignment[s] = r;
					matched++;
					found = true;
					break;
				}
			}
			if (!found) return null;
		}
		return matched == ingredients.size() ? assignment : null;
	}

	@Override
	public ItemStack assemble(CondenserRecipeInput input) {
		return result.create();
	}

	public ItemStack getResultStack() {
		return result.create();
	}

	/** Ingredients, then the modifier if any, then the bottle. */
	public List<Ingredient> getAllIngredients() {
		List<Ingredient> list = new ArrayList<>(ingredients);
		modifier.ifPresent(list::add);
		list.add(bottle);
		return list;
	}

	@Override
	public RecipeSerializer<CondenserRecipe> getSerializer() {
		return ModRecipes.CONDENSER_SERIALIZER.get();
	}

	@Override
	public RecipeType<CondenserRecipe> getType() {
		return ModRecipes.CONDENSER.get();
	}
}
