package nadiendev.rusticrevived.blockentity.alchemy;

import java.util.Optional;

import nadiendev.rusticrevived.recipe.EvaporatingBasinRecipe;
import nadiendev.rusticrevived.recipe.FluidRecipeInput;
import nadiendev.rusticrevived.registry.ModBlockEntities;
import nadiendev.rusticrevived.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Drying basin (legacy TileEntityEvaporatingBasin): the fluid slowly evaporates (the recipe
 * amount per recipe time, 1 mB/tick by default) and once enough of it has evaporated the recipe
 * result appears in the basin.
 */
public class EvaporatingBasinBlockEntity extends SyncedBlockEntity {
	public static final int CAPACITY = 6000;
	private static final int UPDATE_INTERVAL = 20;

	private final AlchemyItems items = new AlchemyItems(1, slot -> sync());
	/** Automation can only take the dried result out. */
	private final ResourceHandler<ItemResource> extractOnly = new AutomationItemHandler(items, (slot, stack) -> false);
	private final AlchemyTank tank = new AlchemyTank(CAPACITY, fluid -> findRecipe(fluid).isPresent(), this::sync);

	/** Fluid that already evaporated but did not form a result yet. */
	private FluidStack evaporated = FluidStack.EMPTY;
	/** Rounding remainder of the evaporation rate. */
	private int remainder;

	public EvaporatingBasinBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.EVAPORATING_BASIN.get(), pos, state);
	}

	public AlchemyItems getItems() {
		return items;
	}

	public ResourceHandler<ItemResource> getAutomationHandler() {
		return extractOnly;
	}

	public AlchemyTank getTank() {
		return tank;
	}

	/** Recipes only exist on the server: the client never fills the basin itself. */
	private Optional<RecipeHolder<EvaporatingBasinRecipe>> findRecipe(FluidStack fluid) {
		if (!(level instanceof ServerLevel serverLevel) || fluid.isEmpty()) return Optional.empty();
		return serverLevel.recipeAccess().getRecipeFor(ModRecipes.EVAPORATING_BASIN.get(), new FluidRecipeInput(fluid), serverLevel);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, EvaporatingBasinBlockEntity basin) {
		if (level.getGameTime() % UPDATE_INTERVAL == 0) {
			basin.evaporate();
		}
	}

	private void evaporate() {
		if (tank.isEmpty() || !evaporated.isEmpty() && !FluidStack.isSameFluid(evaporated, tank.getFluid())) {
			// the fluid changed: start over
			evaporated = FluidStack.EMPTY;
			remainder = 0;
		}
		findRecipe(tank.getFluid()).ifPresent(holder -> {
			EvaporatingBasinRecipe recipe = holder.value();
			int progress = UPDATE_INTERVAL * recipe.getAmount() + remainder;
			remainder = progress % recipe.getTime();
			FluidStack drained = tank.drain(progress / recipe.getTime(), false);
			if (evaporated.isEmpty()) {
				evaporated = drained;
			} else {
				evaporated.grow(drained.getAmount());
			}
			setChanged();
		});
		if (!evaporated.isEmpty()) {
			findRecipe(evaporated).ifPresent(holder -> {
				EvaporatingBasinRecipe recipe = holder.value();
				ItemStack result = recipe.assemble(new FluidRecipeInput(evaporated));
				if (evaporated.getAmount() >= recipe.getAmount() && items.insertItem(0, result, true).isEmpty()) {
					evaporated.shrink(recipe.getAmount());
					items.insertItem(0, result, false);
				}
			});
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		dropContents(items);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putChild("Items", items);
		output.putChild("Tank", tank);
		if (!evaporated.isEmpty()) {
			output.store("EvaporatedFluid", FluidStack.CODEC, evaporated);
		}
		output.putInt("Remainder", remainder);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.readChild("Items", items);
		input.readChild("Tank", tank);
		evaporated = input.read("EvaporatedFluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
		remainder = input.getIntOr("Remainder", 0);
	}
}
