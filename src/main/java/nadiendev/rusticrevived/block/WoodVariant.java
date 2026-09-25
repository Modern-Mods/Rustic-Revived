package nadiendev.rusticrevived.block;

import java.util.function.Supplier;

import nadiendev.rusticrevived.registry.ModBlocks;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

/**
 * Every wood Rustic makes chairs and tables out of.
 */
public enum WoodVariant implements StringRepresentable {
	OAK("oak", () -> Blocks.OAK_PLANKS, true, SoundType.WOOD),
	SPRUCE("spruce", () -> Blocks.SPRUCE_PLANKS, true, SoundType.WOOD),
	BIRCH("birch", () -> Blocks.BIRCH_PLANKS, true, SoundType.WOOD),
	JUNGLE("jungle", () -> Blocks.JUNGLE_PLANKS, true, SoundType.WOOD),
	ACACIA("acacia", () -> Blocks.ACACIA_PLANKS, true, SoundType.WOOD),
	DARK_OAK("dark_oak", () -> Blocks.DARK_OAK_PLANKS, true, SoundType.WOOD),
	MANGROVE("mangrove", () -> Blocks.MANGROVE_PLANKS, true, SoundType.WOOD),
	CHERRY("cherry", () -> Blocks.CHERRY_PLANKS, true, SoundType.CHERRY_WOOD),
	BAMBOO("bamboo", () -> Blocks.BAMBOO_PLANKS, true, SoundType.BAMBOO_WOOD),
	CRIMSON("crimson", () -> Blocks.CRIMSON_PLANKS, false, SoundType.NETHER_WOOD),
	WARPED("warped", () -> Blocks.WARPED_PLANKS, false, SoundType.NETHER_WOOD),
	OLIVE("olive", () -> ModBlocks.OLIVE_PLANKS.get(), true, SoundType.WOOD),
	IRONWOOD("ironwood", () -> ModBlocks.IRONWOOD_PLANKS.get(), true, SoundType.WOOD);

	private final String name;
	private final Supplier<Block> planks;
	private final boolean flammable;
	private final SoundType sound;

	WoodVariant(String name, Supplier<Block> planks, boolean flammable, SoundType sound) {
		this.name = name;
		this.planks = planks;
		this.flammable = flammable;
		this.sound = sound;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public Block planks() {
		return planks.get();
	}

	public boolean isFlammable() {
		return flammable;
	}

	public SoundType sound() {
		return sound;
	}

	/** Texture of the planks, e.g. {@code minecraft:block/oak_planks} or {@code rusticrevived:block/planks_olive}. */
	public Identifier planksTexture() {
		return switch (this) {
			case OLIVE -> Identifier.fromNamespaceAndPath(nadiendev.rusticrevived.RusticRevived.NAMESPACE, "block/planks_olive");
			case IRONWOOD -> Identifier.fromNamespaceAndPath(nadiendev.rusticrevived.RusticRevived.NAMESPACE, "block/planks_ironwood");
			default -> Identifier.withDefaultNamespace("block/" + name + "_planks");
		};
	}
}
