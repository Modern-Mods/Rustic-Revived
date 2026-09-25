package nadiendev.rusticrevived.blockentity.alchemy;

import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

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

	private final FluidTank tank = new FluidTank(CAPACITY, LiquidBarrelBlockEntity::canHold) {
		@Override
		protected void onContentsChanged() {
			sync();
		}
	};

	public LiquidBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.LIQUID_BARREL.get(), pos, state);
	}

	/** Whether a liquid barrel (block or item) may hold the fluid. */
	public static boolean canHold(FluidStack fluid) {
		return !fluid.getFluidType().isLighterThanAir() && fluid.getFluidType().getTemperature(fluid) <= MAX_TEMPERATURE;
	}

	public FluidTank getTank() {
		return tank;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put(TANK_TAG, tank.writeToNBT(registries, new CompoundTag()));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		tank.readFromNBT(registries, tag.getCompound(TANK_TAG));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		tank.setFluid(input.getOrDefault(ModDataComponents.FLUID, SimpleFluidContent.EMPTY).copy());
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if (!tank.isEmpty()) {
			components.set(ModDataComponents.FLUID, SimpleFluidContent.copyOf(tank.getFluid()));
		}
	}

	@Override
	public void removeComponentsFromTag(CompoundTag tag) {
		super.removeComponentsFromTag(tag);
		tag.remove(TANK_TAG);
	}
}
