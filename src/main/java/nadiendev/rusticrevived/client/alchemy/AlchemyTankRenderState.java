package nadiendev.rusticrevived.client.alchemy;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Render state of the alchemy tanks (crushing tub, drying basin, liquid barrel): the held item
 * pile and the fluid.
 */
public class AlchemyTankRenderState extends BlockEntityRenderState {
	/** Model of the held stack (empty when there is none). */
	public final ItemStackRenderState item = new ItemStackRenderState();
	/** Number of item layers of the pile. */
	public int itemLayers;
	/** Seed of the random rotation of the item layers. */
	public long seed;
	public FluidStack fluid = FluidStack.EMPTY;
	/** Fill ratio (0..1) of the tank. */
	public float fill;
}
