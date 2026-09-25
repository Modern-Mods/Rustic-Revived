package nadiendev.rusticrevived.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * Drying basin: evaporates a fluid into an item.
 *
 * <pre>
 * {
 *   "type": "rusticrevived:evaporating_basin",
 *   "fluid": {"ingredient": "rusticrevived:ironberry_juice", "amount": 500},
 *   "result": {"id": "rusticrevived:tiny_iron_dust"},
 *   "time": 500      // optional, defaults to the fluid amount (legacy behaviour)
 * }
 * </pre>
 */
public record EvaporatingBasinRecipe(SizedFluidIngredient fluid, ItemStackTemplate result, int time) implements RusticMachineRecipe<FluidRecipeInput> {

	public static final MapCodec<EvaporatingBasinRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			SizedFluidIngredient.CODEC.fieldOf("fluid").forGetter(EvaporatingBasinRecipe::fluid),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(EvaporatingBasinRecipe::result),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("time", 0).forGetter(EvaporatingBasinRecipe::time)
	).apply(i, EvaporatingBasinRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EvaporatingBasinRecipe> STREAM_CODEC = StreamCodec.composite(
			SizedFluidIngredient.STREAM_CODEC, EvaporatingBasinRecipe::fluid,
			ItemStackTemplate.STREAM_CODEC, EvaporatingBasinRecipe::result,
			ByteBufCodecs.VAR_INT, EvaporatingBasinRecipe::time,
			EvaporatingBasinRecipe::new);

	/** Only checks the fluid type; the basin waits until it holds {@link #getAmount()} mB. */
	@Override
	public boolean matches(FluidRecipeInput input, Level level) {
		return fluid.ingredient().test(input.fluid());
	}

	public int getAmount() {
		return fluid.amount();
	}

	/** Ticks needed to dry {@link #getAmount()} mB. */
	public int getTime() {
		return time > 0 ? time : fluid.amount();
	}

	@Override
	public ItemStack assemble(FluidRecipeInput input) {
		return result.create();
	}

	public ItemStack getResultStack() {
		return result.create();
	}

	@Override
	public RecipeSerializer<EvaporatingBasinRecipe> getSerializer() {
		return ModRecipes.EVAPORATING_BASIN_SERIALIZER.get();
	}

	@Override
	public RecipeType<EvaporatingBasinRecipe> getType() {
		return ModRecipes.EVAPORATING_BASIN.get();
	}
}
