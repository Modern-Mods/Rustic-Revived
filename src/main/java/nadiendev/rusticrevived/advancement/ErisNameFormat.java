package nadiendev.rusticrevived.advancement;

import java.util.UUID;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * A little tribute to Rustic's author (legacy EventHandlerCommon#onNameFormatEvent).
 */
@EventBusSubscriber(modid = RusticRevived.MODID)
public final class ErisNameFormat {
	private static final UUID ERIS_UUID = UUID.fromString("8167f7a5-2db0-42ca-b177-ebc1396dbe55");

	private ErisNameFormat() {
	}

	@SubscribeEvent
	public static void onNameFormat(PlayerEvent.NameFormat event) {
		if (ERIS_UUID.equals(event.getEntity().getUUID())) {
			event.setDisplayname(Component.literal("Mommy Eris").withStyle(ChatFormatting.DARK_RED));
		}
	}
}
