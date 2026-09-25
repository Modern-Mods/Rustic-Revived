package nadiendev.rusticrevived.registry;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item / fluid stack components replacing the legacy NBT tags.
 */
public final class ModDataComponents {
	public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RusticRevived.NAMESPACE);

	/** Fluid held by a fluid bottle or a liquid barrel item (legacy "Fluid" / BlockEntityTag NBT). */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> FLUID = COMPONENTS.registerComponentType("fluid",
			b -> b.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));

	/** Quality (0..1) of a booze {@link net.neoforged.neoforge.fluids.FluidStack} (legacy "Quality" NBT). */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> BOOZE_QUALITY = COMPONENTS.registerComponentType("booze_quality",
			b -> b.persistent(Codec.floatRange(0F, 1F)).networkSynchronized(ByteBufCodecs.FLOAT));

	/** Food that has been drizzled with olive oil: restores extra hunger. */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> OLIVE_OILED = COMPONENTS.registerComponentType("olive_oiled",
			b -> b.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

	/** Weapon coated with vanta oil: applies its effects on hit a limited number of times. */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<VantaOil>> VANTA_OIL = COMPONENTS.registerComponentType("vanta_oil",
			b -> b.persistent(VantaOil.CODEC).networkSynchronized(VantaOil.STREAM_CODEC));

	/** Planks item a cabinet was crafted from; drives the cabinet texture. */
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Item>> CABINET_MATERIAL = COMPONENTS.registerComponentType("cabinet_material",
			b -> b.persistent(BuiltInRegistries.ITEM.byNameCodec()).networkSynchronized(ByteBufCodecs.registry(Registries.ITEM)));

	private ModDataComponents() {
	}

	/**
	 * @param effects effects applied to the struck entity
	 * @param uses    remaining hits
	 */
	public record VantaOil(List<MobEffectInstance> effects, int uses) {
		public static final Codec<VantaOil> CODEC = RecordCodecBuilder.create(i -> i.group(
				MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(VantaOil::effects),
				ExtraCodecs.NON_NEGATIVE_INT.fieldOf("uses").forGetter(VantaOil::uses)
		).apply(i, VantaOil::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, VantaOil> STREAM_CODEC = StreamCodec.composite(
				MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()), VantaOil::effects,
				ByteBufCodecs.VAR_INT, VantaOil::uses,
				VantaOil::new);

		public VantaOil withUses(int newUses) {
			return new VantaOil(effects, newUses);
		}
	}
}
