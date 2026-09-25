package nadiendev.rusticrevived.blockentity.alchemy;

import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Liquid barrel tank (legacy TileEntityLiquidBarrel): 16 buckets of any fluid that is neither
 * gaseous nor too hot. Its content is kept in the item ({@link ModDataComponents#FLUID}) when
 * the barrel is broken.
 */
public class LiquidBarrelBlockEntity extends SyncedBlockEntity {
	public static final int CAPACITY = 16000;
	/** Hottest fluid (in kelvin) a wooden barrel can hold. */
	public static final int MAX_TEMPERATURE = 573;
	private static final String TANK_TAG = "Tank";

	private final AlchemyTank tank = new AlchemyTank(CAPACITY, LiquidBarrelBlockEntity::canHold, this::sync);

	public LiquidBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LIQUID_BARREL.get(), pos, state);
	}

	/** Whether a liquid barrel (block or item) may hold the fluid. */
	public static boolean canHold(FluidStack fluid) {
		return !fluid.getFluidType().isLighterThanAir() && fluid.getFluidType().getTemperature(fluid) <= MAX_TEMPERATURE;
	}

	public AlchemyTank getTank() {
		return tank;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putChild(TANK_TAG, tank);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.readChild(TANK_TAG, tank);
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		tank.setFluid(components.getOrDefault(ModDataComponents.FLUID.get(), SimpleFluidContent.EMPTY).copy());
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if (!tank.isEmpty()) {
			components.set(ModDataComponents.FLUID.get(), SimpleFluidContent.copyOf(tank.getFluid()));
		}
	}

	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		super.removeComponentsFromTag(output);
		output.discard(TANK_TAG);
	}
}
