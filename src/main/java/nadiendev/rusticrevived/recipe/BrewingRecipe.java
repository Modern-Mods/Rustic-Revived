package nadiendev.rusticrevived.recipe;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.fluid.BoozeFluidType;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

/**
 * Brewing barrel: slowly ferments a fluid into booze, one mB at a time.
 * <p>
 * The quality of the result is random, unless the barrel holds a culture of the same booze, in
 * which case the new brew is close to the culture's quality (see the brewing quality config).
 *
 * <pre>
 * {
 *   "type": "rusticrevived:brewing",
 *   "input": {"fluid": "rusticrevived:grape_juice"},
 *   "result": {"id": "rusticrevived:wine", "amount": 1},
 *   "quality": "standard"     // optional: standard | ambrosia
 * }
 * </pre>
 */
public record BrewingRecipe(FluidIngredient input, FluidStack result, QualityMode quality) implements Recipe<FluidRecipeInput> {

	public static final MapCodec<BrewingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			FluidIngredient.CODEC_NON_EMPTY.fieldOf("input").forGetter(BrewingRecipe::input),
			FluidStack.CODEC.fieldOf("result").forGetter(BrewingRecipe::result),
			QualityMode.CODEC.optionalFieldOf("quality", QualityMode.STANDARD).forGetter(BrewingRecipe::quality)
	).apply(i, BrewingRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> STREAM_CODEC = StreamCodec.composite(
			FluidIngredient.STREAM_CODEC, BrewingRecipe::input,
			FluidStack.STREAM_CODEC, BrewingRecipe::result,
			QualityMode.STREAM_CODEC, BrewingRecipe::quality,
			BrewingRecipe::new);

	@Override
	public boolean matches(FluidRecipeInput input, Level level) {
		return this.input.test(input.fluid());
	}

	/**
	 * Whether the auxiliary (culture) tank is compatible with this recipe: empty, or holding the
	 * result fluid.
	 */
	public boolean matchesCulture(FluidStack culture) {
		return culture.isEmpty() || culture.getFluid() == result.getFluid();
	}

	/**
	 * Creates a stack of the resulting booze with a freshly rolled quality.
	 *
	 * @param culture content of the culture tank (may be empty)
	 * @param amount  amount of the returned stack
	 */
	public FluidStack brew(@Nullable FluidStack culture, int amount, RandomSource rand) {
		FluidStack out = result.copyWithAmount(amount);
		if (!(out.getFluidType() instanceof BoozeFluidType)) {
			return out;
		}
		if (culture != null && !culture.isEmpty() && culture.getFluid() == out.getFluid() && BoozeFluidType.hasQuality(culture)) {
			float cultureQuality = BoozeFluidType.getQuality(culture);
			int min = RusticConfig.COMMON.minBrewQualityChange.get();
			int max = Math.max(min, RusticConfig.COMMON.maxBrewQualityChange.get());
			if (quality == QualityMode.AMBROSIA) {
				min = Math.max(0, min);
				max = Math.max(7, max);
			}
			int change = rand.nextInt(max - min + 1) + min;
			return BoozeFluidType.withQuality(out, Math.max(Math.min((change + (int) (100 * cultureQuality)) / 100F, 1F), 0F));
		}
		float q;
		if (quality == QualityMode.AMBROSIA) {
			int r = rand.nextInt(4) == 0 ? rand.nextInt(26) : rand.nextInt(12) + 14;
			q = (49 + r) / 100F;
		} else {
			q = (5 + rand.nextInt(71)) / 100F;
		}
		return BoozeFluidType.withQuality(out, q);
	}

	@Override
	public ItemStack assemble(FluidRecipeInput input, HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.BREWING_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipes.BREWING.get();
	}

	public enum QualityMode implements StringRepresentable {
		/** 5% - 75% without culture. */
		STANDARD("standard"),
		/** 49% - 74%, usually above 63%, without culture; cultures never degrade. */
		AMBROSIA("ambrosia");

		public static final Codec<QualityMode> CODEC = StringRepresentable.fromEnum(QualityMode::values);
		public static final StreamCodec<io.netty.buffer.ByteBuf, QualityMode> STREAM_CODEC = ByteBufCodecs.idMapper(
				ByIdMap.continuous(QualityMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), QualityMode::ordinal);

		private final String name;

		QualityMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}
}
