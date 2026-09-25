package nadiendev.rusticrevived.client.storage;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

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
		ItemStackRenderState renderState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, new ItemStack(item), ItemDisplayContext.GUI, null, null, 0);
		Material.Baked particle = renderState.pickParticleMaterial(RandomSource.create(0L));
		if (particle == null) return 0xFFFFFFFF;
		SpriteContents sprite = particle.sprite().contents();
		NativeImage image = sprite.getOriginalImage();
		long red = 0;
		long green = 0;
		long blue = 0;
		int pixels = 0;
		for (int y = 0; y < sprite.height(); y++) {
			for (int x = 0; x < sprite.width(); x++) {
				int argb = image.getPixel(x, y);
				if (argb >>> 24 == 0) continue;
				red += argb >> 16 & 0xFF;
				green += argb >> 8 & 0xFF;
				blue += argb & 0xFF;
				pixels++;
			}
		}
		if (pixels == 0) return 0xFFFFFFFF;
		return 0xFF000000 | (int) (red / pixels) << 16 | (int) (green / pixels) << 8 | (int) (blue / pixels);
	}
}
