package nadiendev.rusticrevived.client.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
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

	private final Map<ResourceLocation, Optional<ResourceLocation>> cache = new HashMap<>();
	private int nextId;

	private IronSkinTextures() {
	}

	/** The metal texture matching {@code entityTexture}, or null if it could not be built. Render thread only. */
	@Nullable
	public ResourceLocation get(ResourceLocation entityTexture) {
		return cache.computeIfAbsent(entityTexture, this::create).orElse(null);
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		TextureManager textures = Minecraft.getInstance().getTextureManager();
		cache.values().forEach(texture -> texture.ifPresent(textures::release));
		cache.clear();
	}

	private Optional<ResourceLocation> create(ResourceLocation entityTexture) {
		RenderSystem.assertOnRenderThread();
		try (NativeImage base = readEntityTexture(entityTexture)) {
			int width = base.getWidth();
			int height = base.getHeight();
			ResourceLocation metalLocation = RusticRevived.id("textures/entity/layer/fullmetal_" + sizeClass(width) + "_" + sizeClass(height) + ".png");
			try (NativeImage metal = readResource(metalLocation)) {
				NativeImage combined = new NativeImage(width, height, true);
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int alpha = base.getPixelRGBA(x, y) & 0xFF000000;
						int color = metal.getPixelRGBA(x * metal.getWidth() / width, y * metal.getHeight() / height) & 0x00FFFFFF;
						combined.setPixelRGBA(x, y, alpha | color);
					}
				}
				ResourceLocation id = RusticRevived.id("dynamic/iron_skin_" + nextId++);
				Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(combined));
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

	private static NativeImage readResource(ResourceLocation location) throws IOException {
		Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location);
		try (InputStream stream = resource.open()) {
			return NativeImage.read(stream);
		}
	}

	/** Reads the texture from the resource packs, or from the GPU for runtime textures (downloaded skins). */
	private static NativeImage readEntityTexture(ResourceLocation location) throws IOException {
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
		if (resource.isPresent()) {
			try (InputStream stream = resource.get().open()) {
				return NativeImage.read(stream);
			}
		}
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(location);
		texture.bind();
		int width = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		int height = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		if (width <= 0 || height <= 0) throw new IOException("Texture " + location + " has no image");
		NativeImage image = new NativeImage(width, height, false);
		image.downloadTexture(0, false);
		return image;
	}
}
