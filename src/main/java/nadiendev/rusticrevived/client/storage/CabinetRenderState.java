package nadiendev.rusticrevived.client.storage;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;

/**
 * What {@link CabinetRenderer} needs to draw a cabinet, extracted from its block entity.
 */
public class CabinetRenderState extends BlockEntityRenderState {
	/** False for the upper half of a double cabinet (drawn by the lower one). */
	public boolean visible;
	public boolean mirror;
	public boolean isDouble;
	public Direction facing = Direction.SOUTH;
	/** Door rotation around the hinge, in radians. */
	public float doorAngle;
	public @Nullable Item material;
}
