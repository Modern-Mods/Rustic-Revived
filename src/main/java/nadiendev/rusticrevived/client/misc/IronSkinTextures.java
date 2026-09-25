package nadiendev.rusticrevived.client.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * Builds the textures of the iron skin layer. The legacy layer multi-textured the entity model with a
 * metal texture ({@code entity/layer/fullmetal_<w>_<h>.png}, picked by the size of the entity texture)
 * masked by the alpha of the entity texture; here that combination is baked once per entity texture
 * into a dynamic texture: metal colors with the entity texture's alpha.
 */
public final class IronSkinTextures implements ResourceManagerReloadListener {
	public static final IronSkinTextures INSTANCE = new IronSkinTextures();

	private final Map<Identifier, Optional<Identifier>> cache = new HashMap<>();
	private int nextId;

	private IronSkinTextures() {
	}

	/** The metal texture matching {@code entityTexture}, or null if it could not be built. Render thread only. */
	@Nullable
	public Identifier get(Identifier entityTexture) {
		return cache.computeIfAbsent(entityTexture, this::create).orElse(null);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		TextureManager textures = Minecraft.getInstance().getTextureManager();
		cache.values().forEach(texture -> texture.ifPresent(textures::release));
		cache.clear();
	}

	private Optional<Identifier> create(Identifier entityTexture) {
		RenderSystem.assertOnRenderThread();
		try (NativeImage base = readEntityTexture(entityTexture)) {
			int width = base.getWidth();
			int height = base.getHeight();
			Identifier metalLocation = RusticRevived.id("textures/entity/layer/fullmetal_" + sizeClass(width) + "_" + sizeClass(height) + ".png");
			try (NativeImage metal = readResource(metalLocation)) {
				NativeImage combined = new NativeImage(width, height, true);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int alpha = base.getPixel(x, y) & 0xFF000000;
						int color = metal.getPixel(x * metal.getWidth() / width, y * metal.getHeight() / height) & 0x00FFFFFF;
						combined.setPixel(x, y, alpha | color);
					}
				}
				Identifier id = RusticRevived.id("dynamic/iron_skin_" + nextId++);
				Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(id::toString, combined));
				return Optional.of(id);
			}
		} catch (IOException | RuntimeException e) {
			RusticRevived.LOGGER.warn("Could not build the iron skin texture of {}", entityTexture, e);
			return Optional.empty();
		}
	}

	/** Legacy texture size classes: 16, 32, 64 or 128. */
	private static int sizeClass(int size) {
		if (size >= 128) return 128;
		if (size >= 64) return 64;
		if (size >= 32) return 32;
		return 16;
	}

	private static NativeImage readResource(Identifier location) throws IOException {
		Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location);
		try (InputStream stream = resource.open()) {
			return NativeImage.read(stream);
		}
	}

	/** Reads the texture from the resource packs, or from the pixels of runtime textures (downloaded skins). */
	private static NativeImage readEntityTexture(Identifier location) throws IOException {
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
		if (resource.isPresent()) {
			try (InputStream stream = resource.get().open()) {
				return NativeImage.read(stream);
			}
		}
		if (Minecraft.getInstance().getTextureManager().getTexture(location) instanceof DynamicTexture dynamic) {
			NativeImage pixels = dynamic.getPixels();
			NativeImage copy = new NativeImage(pixels.format(), pixels.getWidth(), pixels.getHeight(), false);
			copy.copyFrom(pixels);
			return copy;
		}
		throw new IOException("Texture " + location + " has no readable image");
	}
}
