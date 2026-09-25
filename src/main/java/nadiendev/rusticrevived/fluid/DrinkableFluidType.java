package nadiendev.rusticrevived.fluid;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * A fluid that can be drunk from a Rustic fluid bottle (legacy FluidDrinkable).
 */
public class DrinkableFluidType extends FluidType {

	@FunctionalInterface
	public interface DrinkEffect {
		void onDrank(Level level, Player player, ItemStack stack, FluidStack fluid);
	}

	private final DrinkEffect drinkEffect;

	public DrinkableFluidType(Properties properties, DrinkEffect drinkEffect) {
		super(properties);
		this.drinkEffect = drinkEffect;
	}

	/**
	 * Called server side once a player finishes drinking a bottle of this fluid.
	 */
	public void onDrank(Level level, Player player, ItemStack stack, FluidStack fluid) {
		drinkEffect.onDrank(level, player, stack, fluid);
	}
}
