package nadiendev.rusticrevived.client.misc;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.config.RusticConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * HUD additions: an armor bar that grows extra (compressed) rows beyond 20 armor points, and an
 * armor toughness bar above the hunger bar.
 */
public final class ArmorHud {
	public static final Identifier TOUGHNESS_LAYER = RusticRevived.id("armor_toughness");
	private static final Identifier ICONS = RusticRevived.id("textures/gui/icons.png");
	private static final int ICONS_SIZE = 256;
	private static final Identifier ARMOR_EMPTY = Identifier.withDefaultNamespace("hud/armor_empty");
	private static final Identifier ARMOR_HALF = Identifier.withDefaultNamespace("hud/armor_half");
	private static final Identifier ARMOR_FULL = Identifier.withDefaultNamespace("hud/armor_full");

	private ArmorHud() {
	}

	/** Replaces the vanilla armor bar when the extra armor HUD is enabled. */
	public static void onRenderLayer(RenderGuiLayerEvent.Pre event) {
		if (!event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL) || !RusticConfig.CLIENT.extraArmorHud.get()) return;
		Minecraft minecraft = Minecraft.getInstance();
		if (!(minecraft.getCameraEntity() instanceof Player player)) return;
		event.setCanceled(true);
		if (minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) return;

		GuiGraphicsExtractor graphics = event.getGuiGraphics();
		Gui gui = minecraft.gui;
		int armor = player.getArmorValue();
		int rows = Mth.ceil(armor / 20.0F);
		int rowHeight = Math.min(Math.max(10 - (rows - 2), 3), 10);
		int top = graphics.guiHeight() - gui.leftHeight - (rows * rowHeight - 10);
		for (int row = rows - 1; row >= 0; row--) {
			int left = graphics.guiWidth() / 2 - 91;
			for (int i = 1; i < 20; i += 2) {
				int value = i + row * 20;
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, value < armor ? ARMOR_FULL : value == armor ? ARMOR_HALF : ARMOR_EMPTY, left, top, 9, 9);
				left += 8;
			}
			top += rowHeight;
			gui.leftHeight += rowHeight;
		}
		if (rows > 0 && rowHeight < 10) {
			gui.leftHeight += 10 - rowHeight;
		}
	}

	/** GUI layer above the food bar showing the armor toughness of the camera entity. */
	public static void renderToughness(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) return;
		if (!RusticConfig.CLIENT.toughnessHud.get() || !(minecraft.getCameraEntity() instanceof LivingEntity entity)) return;
		int toughness = Mth.floor(entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
		if (toughness <= 0) return;

		Gui gui = minecraft.gui;
		int rows = Mth.ceil(toughness / 20.0F);
		int rowHeight = Math.min(Math.max(10 - (rows - 2), 3), 10);
		int top = graphics.guiHeight() - gui.rightHeight - (rows * rowHeight - 10);
		for (int row = rows - 1; row >= 0; row--) {
			int right = graphics.guiWidth() / 2 + 82;
			for (int i = 1; i < 20; i += 2) {
				int value = i + row * 20;
				graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, right, top, value < toughness ? 34 : value == toughness ? 25 : 16, 0, 9, 9, ICONS_SIZE,
						ICONS_SIZE);
				right -= 8;
			}
			top += rowHeight;
			gui.rightHeight += rowHeight;
		}
		if (rowHeight < 10) {
			gui.rightHeight += 10 - rowHeight;
		}
	}
}
