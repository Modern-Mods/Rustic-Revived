package nadiendev.rusticrevived.compat.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nadiendev.rusticrevived.RusticRevived;
import nadiendev.rusticrevived.config.RusticConfig;
import nadiendev.rusticrevived.recipe.CondenserRecipe;
import nadiendev.rusticrevived.registry.ModBlocks;
import nadiendev.rusticrevived.registry.ModDataComponents;
import nadiendev.rusticrevived.registry.ModFluids;
import nadiendev.rusticrevived.registry.ModItems;
import nadiendev.rusticrevived.registry.ModRecipes;
import nadiendev.rusticrevived.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Builds the {@link CraftingDisplayRecipe}s for Rustic's dynamic crafting recipes (legacy
 * OliveOilRecipeMaker, VantaOilRecipeWrapper#getVantaOilRecipes, CabinetRecipeWrapper#getCabinetRecipes).
 */
public final class CraftingDisplays {
	/** Content of a fluid bottle (see FluidBottleItem). */
	private static final int BOTTLE_AMOUNT = 250;
	/** Weapon + oil bottle + up to 7 elixirs / potions fill the crafting grid. */
	private static final int MAX_VANTA_INGREDIENTS = 7;

	private CraftingDisplays() {
	}

	public static List<RecipeHolder<CraftingRecipe>> create(Collection<RecipeHolder<CondenserRecipe>> condenserRecipes) {
		List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
		if (RusticConfig.COMMON.enableOliveOiling.get()) {
			oliveOiling(recipes);
		}
		vantaOiling(recipes, condenserRecipes);
		cabinet(recipes);
		return recipes;
	}

	/** Any food (not blacklisted) + a bottle of olive oil -> the oiled food. */
	private static void oliveOiling(List<RecipeHolder<CraftingRecipe>> recipes) {
		List<ItemStack> foods = new ArrayList<>();
		List<ItemStack> oiled = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack food = new ItemStack(item);
			if (food.has(DataComponents.FOOD) && !food.is(ModTags.Items.OLIVE_OIL_BLACKLIST)) {
				foods.add(food);
				ItemStack result = food.copy();
				result.set(ModDataComponents.OLIVE_OILED, Unit.INSTANCE);
				oiled.add(result);
			}
		}
		if (foods.isEmpty()) return;
		List<List<ItemStack>> inputs = List.of(foods, List.of(fluidBottle(ModFluids.OLIVE_OIL.get())));
		recipes.add(holder("olive_oiling", new CraftingDisplayRecipe(ModRecipes.OLIVE_OIL_SERIALIZER.get(), inputs, oiled, 0, 0, 0)));
	}

	/**
	 * Weapon + a bottle of vanta oil + 1 to 7 potions / elixirs with a single effect -> the coated
	 * weapon, one display per number of potions.
	 */
	private static void vantaOiling(List<RecipeHolder<CraftingRecipe>> recipes, Collection<RecipeHolder<CondenserRecipe>> condenserRecipes) {
		List<ItemStack> weapons = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack stack = new ItemStack(item);
			if (item instanceof AxeItem || stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ModTags.Items.VANTA_OILABLE)) {
				weapons.add(stack);
			}
		}
		List<ItemStack> potions = new ArrayList<>();
		BuiltInRegistries.POTION.listElements().forEach(potion -> {
			if (potion.value().getEffects().size() == 1) {
				potions.add(PotionContents.createItemStack(Items.POTION, potion));
				potions.add(PotionContents.createItemStack(Items.SPLASH_POTION, potion));
				potions.add(PotionContents.createItemStack(Items.LINGERING_POTION, potion));
			}
		});
		for (RecipeHolder<CondenserRecipe> condenser : condenserRecipes) {
			ItemStack elixir = condenser.value().getResultStack();
			if (elixir.is(ModItems.ELIXIR) && effectCount(elixir) == 1) {
				potions.add(elixir);
			}
		}
		if (weapons.isEmpty() || potions.isEmpty()) return;
		List<ItemStack> vantaBottle = List.of(fluidBottle(ModFluids.VANTA_OIL.get()));
		for (int count = 1; count <= MAX_VANTA_INGREDIENTS; count++) {
			List<List<ItemStack>> inputs = new ArrayList<>();
			inputs.add(weapons);
			inputs.add(vantaBottle);
			inputs.addAll(Collections.nCopies(count, potions));
			recipes.add(holder("vanta_oiling_" + count, new CraftingDisplayRecipe(ModRecipes.VANTA_OIL_SERIALIZER.get(), inputs,
					weapons.stream().map(ItemStack::copy).toList(), 0, 0, 0)));
		}
	}

	/** Cabinet material all around, an empty centre and a wooden trapdoor on a side -> cabinet of that material. */
	private static void cabinet(List<RecipeHolder<CraftingRecipe>> recipes) {
		List<ItemStack> materials = tagStacks(ModTags.Items.CABINET_MATERIALS);
		List<ItemStack> trapdoors = tagStacks(ItemTags.WOODEN_TRAPDOORS);
		if (materials.isEmpty() || trapdoors.isEmpty()) return;
		List<ItemStack> cabinets = new ArrayList<>();
		for (ItemStack material : materials) {
			ItemStack cabinet = new ItemStack(ModBlocks.CABINET);
			cabinet.set(ModDataComponents.CABINET_MATERIAL, material.getItem());
			cabinets.add(cabinet);
		}
		List<List<ItemStack>> grid = List.of(
				materials, materials, materials,
				materials, List.of(), trapdoors,
				materials, materials, materials);
		recipes.add(holder("cabinet", new CraftingDisplayRecipe(ModRecipes.CABINET_SERIALIZER.get(), grid, cabinets, 3, 3,
				0, 1, 2, 3, 6, 7, 8)));
	}

	private static RecipeHolder<CraftingRecipe> holder(String name, CraftingRecipe recipe) {
		return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, RusticRevived.id("jei/" + name)), recipe);
	}

	private static ItemStack fluidBottle(Fluid fluid) {
		ItemStack bottle = new ItemStack(ModItems.FLUID_BOTTLE.get());
		bottle.set(ModDataComponents.FLUID, SimpleFluidContent.copyOf(new FluidStack(fluid, BOTTLE_AMOUNT)));
		return bottle;
	}

	private static List<ItemStack> tagStacks(TagKey<Item> tag) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
			stacks.add(new ItemStack(item));
		}
		return stacks;
	}

	private static int effectCount(ItemStack stack) {
		int count = 0;
		for (MobEffectInstance ignored : stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects()) {
			count++;
		}
		return count;
	}
}
