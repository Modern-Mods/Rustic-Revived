package nadiendev.rusticrevived.network;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.item.VaseItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the player scrolled while sneaking with a vase in the main hand, selecting
 * design {@code variant} (legacy MessageVaseMeta).
 */
public record VaseDesignPayload(int variant) implements CustomPacketPayload {
	public static final Type<VaseDesignPayload> TYPE = new Type<>(RusticRevived.id("vase_design"));
	public static final StreamCodec<RegistryFriendlyByteBuf, VaseDesignPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, VaseDesignPayload::variant, VaseDesignPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(VaseDesignPayload payload, IPayloadContext context) {
		ItemStack stack = context.player().getItemInHand(InteractionHand.MAIN_HAND);
		if (stack.getItem() instanceof VaseItem vase) {
			vase.setDesign(stack, payload.variant());
		}
	}
}
