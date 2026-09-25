package nadiendev.rusticrevived.client.storage;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Average colour of an item's texture, used to tint cabinets made from it (legacy ItemColorCache /
 * ClientUtils#getTextureColor). Cleared on resource reload.
 */
public final class MaterialColors {
	private static final Map<Item, Integer> CACHE = new HashMap<>();

	private MaterialColors() {
	}

	public static void clear() {
		CACHE.clear();
	}

	/** Opaque ARGB average of the non transparent pixels of the item's particle texture. */
	public static int get(Item item) {
		return CACHE.computeIfAbsent(item, MaterialColors::compute);
	}

	private static int compute(Item item) {
		SpriteContents sprite = Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(item), null, null, 0)
				.getParticleIcon(ModelData.EMPTY).contents();
		NativeImage image = sprite.getOriginalImage();
		long red = 0;
		long green = 0;
		long blue = 0;
		int pixels = 0;
		for (int y = 0; y < sprite.height(); y++) {
			for (int x = 0; x < sprite.width(); x++) {
				int abgr = image.getPixelRGBA(x, y);
				if (abgr >>> 24 == 0) continue;
				red += abgr & 0xFF;
				green += abgr >> 8 & 0xFF;
				blue += abgr >> 16 & 0xFF;
				pixels++;
			}
		}
		if (pixels == 0) return 0xFFFFFFFF;
		return 0xFF000000 | (int) (red / pixels) << 16 | (int) (green / pixels) << 8 | (int) (blue / pixels);
	}
}
