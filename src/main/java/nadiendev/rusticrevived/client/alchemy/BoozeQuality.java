package nadiendev.rusticrevived.client.alchemy;

import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client presentation of the booze quality (legacy ClientUtils.getQualityTooltip /
 * getQualityLabelColor). Every player sees one of eight synonyms for each quality tier.
 */
public final class BoozeQuality {
	private static final UUID ERIS_UUID = UUID.fromString("8167f7a5-2db0-42ca-b177-ebc1396dbe55");
	private static final int VARIANTS = 8;

	private BoozeQuality() {
	}

	private enum Tier {
		HIGHEST("highest", 0.89999F, ChatFormatting.GOLD, 0xC29311),
		HIGH("high", 0.69999F, ChatFormatting.LIGHT_PURPLE, 0x4A7ABD),
		HIGHISH("highish", 0.5F, ChatFormatting.AQUA, 0xDBD3BD),
		LOWISH("lowish", 0.35F, ChatFormatting.YELLOW, 0xDBD3BD),
		LOW("low", 0.2F, ChatFormatting.DARK_PURPLE, 0xBAA911),
		LOWEST("lowest", Float.NEGATIVE_INFINITY, ChatFormatting.DARK_RED, 0x222222);

		final String name;
		final float minimum;
		final ChatFormatting textColor;
		final int labelColor;

		Tier(String name, float minimum, ChatFormatting textColor, int labelColor) {
			this.name = name;
			this.minimum = minimum;
			this.textColor = textColor;
			this.labelColor = labelColor;
		}

		static Tier of(float quality) {
			for (Tier tier : values()) {
				if (quality >= tier.minimum) return tier;
			}
			return LOWEST;
		}
	}

	/** "Quality: Tasty [72%]" */
	public static Component tooltip(float quality) {
		Tier tier = Tier.of(quality);
		Component name = Component.translatable("tooltip.rusticrevived.quality." + tier.name + "." + playerVariant()).withStyle(tier.textColor);
		return Component.translatable("tooltip.rusticrevived.quality.desc", name, String.format("%.0f%%", quality * 100)).withStyle(ChatFormatting.GRAY);
	}

	/** Opaque colour of the bottle label for the quality. */
	public static int labelColor(float quality) {
		return 0xFF000000 | Tier.of(quality).labelColor;
	}

	private static int playerVariant() {
		Minecraft minecraft = Minecraft.getInstance();
		UUID id = minecraft.player != null ? minecraft.player.getUUID() : minecraft.getUser().getProfileId();
		if (id == null) return 0;
		return id.equals(ERIS_UUID) ? VARIANTS - 1 : id.hashCode() & (VARIANTS - 1);
	}
}
