package nadiendev.rusticrevived.registry;

import com.mojang.serialization.MapCodec;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.recipe.BrewingRecipe;
import nadiendev.rusticrevived.recipe.CabinetRecipe;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.recipe.ConfigCondition;
import nadiendev.rusticrevived.recipe.CrushingTubRecipe;
import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import nadiendev.rusticrevived.recipe.NoRemainderShapelessRecipe;
import nadiendev.rusticrevived.recipe.OliveOilRecipe;
import nadiendev.rusticrevived.recipe.VantaOilRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModRecipes {
	public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, RusticRevived.NAMESPACE);
	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, RusticRevived.NAMESPACE);
	public static final DeferredRegister<RecipeBookCategory> BOOK_CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, RusticRevived.NAMESPACE);
	public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RusticRevived.NAMESPACE);

	public static final DeferredHolder<RecipeType<?>, RecipeType<CrushingTubRecipe>> CRUSHING_TUB = type("crushing_tub");
	public static final DeferredHolder<RecipeType<?>, RecipeType<EvaporatingBasinRecipe>> EVAPORATING_BASIN = type("evaporating_basin");
	public static final DeferredHolder<RecipeType<?>, RecipeType<CondenserRecipe>> CONDENSER = type("condenser");
	public static final DeferredHolder<RecipeType<?>, RecipeType<BrewingRecipe>> BREWING = type("brewing");

	/** Book category of every Rustic machine recipe (they never show in the vanilla recipe book). */
	public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> MACHINES_BOOK_CATEGORY = BOOK_CATEGORIES.register("machines", RecipeBookCategory::new);

	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrushingTubRecipe>> CRUSHING_TUB_SERIALIZER = SERIALIZERS.register("crushing_tub",
			() -> new RecipeSerializer<>(CrushingTubRecipe.CODEC, CrushingTubRecipe.STREAM_CODEC));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EvaporatingBasinRecipe>> EVAPORATING_BASIN_SERIALIZER = SERIALIZERS.register("evaporating_basin",
			() -> new RecipeSerializer<>(EvaporatingBasinRecipe.CODEC, EvaporatingBasinRecipe.STREAM_CODEC));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CondenserRecipe>> CONDENSER_SERIALIZER = SERIALIZERS.register("condenser",
			() -> new RecipeSerializer<>(CondenserRecipe.CODEC, CondenserRecipe.STREAM_CODEC));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BrewingRecipe>> BREWING_SERIALIZER = SERIALIZERS.register("brewing",
			() -> new RecipeSerializer<>(BrewingRecipe.CODEC, BrewingRecipe.STREAM_CODEC));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NoRemainderShapelessRecipe>> NO_REMAINDER_SHAPELESS_SERIALIZER = SERIALIZERS.register(
			"shapeless_no_remainder", () -> new RecipeSerializer<>(NoRemainderShapelessRecipe.CODEC, NoRemainderShapelessRecipe.STREAM_CODEC));
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<OliveOilRecipe>> OLIVE_OIL_SERIALIZER = special("olive_oil", OliveOilRecipe.INSTANCE);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<VantaOilRecipe>> VANTA_OIL_SERIALIZER = special("vanta_oil", VantaOilRecipe.INSTANCE);
	public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CabinetRecipe>> CABINET_SERIALIZER = special("cabinet", CabinetRecipe.INSTANCE);

	public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCondition>> CONFIG_CONDITION =
			CONDITIONS.register("config", () -> ConfigCondition.CODEC);

	private ModRecipes() {
	}

	private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> type(String name) {
		return TYPES.register(name, () -> RecipeType.simple(RusticRevived.id(name)));
	}

	/** Serializer of a special crafting recipe without data (a singleton). */
	private static <T extends Recipe<?>> DeferredHolder<RecipeSerializer<?>, RecipeSerializer<T>> special(String name, T instance) {
		return SERIALIZERS.register(name, () -> new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance)));
	}
}
