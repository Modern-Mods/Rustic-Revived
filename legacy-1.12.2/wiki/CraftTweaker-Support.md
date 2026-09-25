Rustic now includes CraftTweaker Integration so that modpack makers can add and remove recipes for my custom crafting systems.

***

## Crushing Tub Recipes ##
To add recipes, use  
    `mods.rustic.CrushingTub.addRecipe(output, byproduct, input);`  
where _output_ is a fluidstack, and _byproduct_ and _input_ are both itemstacks. _byproduct_ can be `null`.  
To remove recipes, use any of the following methods:
```
mods.rustic.CrushingTub.removeRecipe(output, input);
mods.rustic.CrushingTub.removeRecipe(output);
mods.rustic.CrushingTub.removeRecipe(input);
```  
where _output_ is a fluidstack, and _input_ is an itemstack.

## Evaporating Basin Recipes ##
To add recipes, use either of the following methods:
```
mods.rustic.EvaporatingBasin.addRecipe(output, input);
mods.rustic.EvaporatingBasin.addRecipe(output, input, time);
```
where _output_ is an itemstack, and _input_ is a fluidstack, and _time_ is the number of ticks it takes for the input to evaporate.  
To remove recipes, use either of the following methods:
```
mods.rustic.EvaporatingBasin.removeRecipe(output);
mods.rustic.EvaporatingBasin.removeRecipe(input);
```  
where _output_ is a fluidstack, and _input_ is a fluidstack.

## Alchemy Recipes ##
As of version 0.3.12, in order to use elixir itemstacks with custom effects in your scripts, you need to use the new format for elixirs. The format is:

    {ElixirEffects:[{Effect:"[effect registry name]", Duration:[insert duration here], Amplifier:[insert amplifier here]}]}

Alchemy recipes are divided in *simple* recipe and *advanced* recipe. A simple recipe can be performed in the standard condenser, while an advanced recipe requires the advanced condenser.

In all the following methods, a recipe is *simple* if it has two ingredients or less and no modifier.
>If you wish to force a 2-ingredient modifier-less recipe to require the advanced condenser, you can add a first ingredients as `null` to have a 3-ingredients recipe, where one ingredient is ignored.

To add simple alchemy recipes (2 ingredients or fewer, no modifier), you can use the simple method:
```
mods.rustic.Condenser.addRecipe(IItemStack output, IIngredient input1, IIngredient input2);
```
where:
+ `output` is the output of the recipe
+ `input1` is the first input
+ `input2` is the second input

Recipes added that way automatically fulfill the simple recipe criteria, though you have less control.

> Before version 1.1.3, the inputs must be `IItemStack`
> In version 1.1.3, full `IIngredient` support was added

For more control over the modifier, bottle, the cooking time and the fluid required, the full method is:
> This method is only available on version 1.1.3+
```
 mods.rustic.Condenser.addRecipe(IItemStack output, IIngredients[] inputs, @optional IIngredient modifier, @option IIngredient bottle,@optional ILiquidStack fluid, @optional int time);
```
where
+ `output` is the recipe output
+ `inputs` is an array of at most 3 inputs
+ `modifier` is the modifier required, can be `null` or left-out (in which case it's `null`)
+ `bottle` is the bottle required in the bottle slot. If left out, it defaults to a vanilla glass bottle. If `null`, no bottle is required. (*WARNING*: do not use items that can be used as fuel in a furnace as bottles, otherwise the recipe cannot be automated. See the Alchemy page for details)
+ `fluid` is the fluid and amount to consume. If `null`or left out, it defaults to 125mb of water (that is, `<liquid:water> * 125` in CrT syntax)
+ `time` is the time in ticks that the recipe takes to complete, defaults to 400 if left out.

All optional parameters can be left out, but cannot be skipped. You can use `null` to leave an optional parameter (but not `time`, see also `bottle`) to its default.

In short, you can use the method like so:
```
mods.rustic.Condenser.addRecipe(output, itemstack[] inputs);
mods.rustic.Condenser.addRecipe(output, itemstack[] inputs, modifier);
mods.rustic.Condenser.addRecipe(output, itemstack[] inputs, modifier, bottle);
mods.rustic.Condenser.addRecipe(output, itemstack[] inputs, modifier, bottle, fluid);
mods.rustic.Condenser.addRecipe(output, itemstack[] inputs, modifier, bottle, fluid, time);
```

To remove recipes, use
```
mods.rustic.Condenser.removeRecipe(IItemStack output);
```
where _output_ is the output for which to remove recipes.

## Examples of Recipe Additions ##
#### Crushing Tub ####
```
mods.rustic.CrushingTub.addRecipe(<liquid:water> * 125, null, <minecraft:melon> * 1);
```
#### Drying Basin ####
```
mods.rustic.EvaporatingBasin.addRecipe(<minecraft:sugar> * 1, <liquid:oliveoil> * 750);
```
#### Alchemy ####
```
val slownessElixir = <rustic:elixir>.withTag({ElixirEffects: [{Effect: "minecraft:slowness", Duration: 1800, Amplifier: 0}]});
val slownessExtendedElixir = <rustic:elixir>.withTag({ElixirEffects: [{Effect: "minecraft:slowness", Duration: 4800, Amplifier: 0}]});

mods.rustic.Condenser.addRecipe(slownessElixir, <minecraft:web>, <minecraft:vine>);
mods.rustic.Condenser.addRecipe(slownessExtendedElixir, [<minecraft:web>, <minecraft:vine>], <rustic:horsetail>);
```
