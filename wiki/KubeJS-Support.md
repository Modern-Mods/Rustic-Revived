Rustic Revived includes [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs) integration (1.21.1 and 26.1.2) so that modpack makers can add and remove recipes for its custom crafting systems. All recipes go in a `server_scripts` file inside the `ServerEvents.recipes` event.

The recipe types are `rusticrevived:crushing_tub`, `rusticrevived:evaporating_basin`, `rusticrevived:condenser` and `rusticrevived:brewing`. Arguments are given in the order shown below; optional arguments can be left out or set with the method of the same name.

***

## Crushing Tub ##
`event.recipes.rusticrevived.crushing_tub(result, ingredient, byproduct?)`
+ `result`: the fluid produced
+ `ingredient`: the item crushed
+ `byproduct` (optional): an item dropped above the tub

```js
ServerEvents.recipes(event => {
  event.recipes.rusticrevived.crushing_tub(Fluid.of('minecraft:water', 125), 'minecraft:melon_slice')
  event.recipes.rusticrevived.crushing_tub(Fluid.of('minecraft:water', 250), 'minecraft:pumpkin', 'minecraft:pumpkin_seeds')
})
```

## Evaporating Basin ##
`event.recipes.rusticrevived.evaporating_basin(result, fluid, time?)`
+ `result`: the item produced
+ `fluid`: the fluid and amount evaporated
+ `time` (optional): ticks the recipe takes; if left out it takes one tick per mB

```js
ServerEvents.recipes(event => {
  event.recipes.rusticrevived.evaporating_basin('minecraft:sugar', Fluid.of('rusticrevived:olive_oil', 750))
  event.recipes.rusticrevived.evaporating_basin('minecraft:clay_ball', Fluid.of('minecraft:water', 1000)).time(400)
})
```

## Condenser (Alchemy) ##
`event.recipes.rusticrevived.condenser(result, ingredients, modifier?, bottle?, fluid?, time?, advanced?)`
+ `result`: the item produced, usually an elixir
+ `ingredients`: a list of 1 to 3 ingredients
+ `modifier` (optional): the modifier ingredient; recipes with a modifier or 3 ingredients need the advanced condenser
+ `bottle` (optional): defaults to a glass bottle
+ `fluid` (optional): defaults to 125 mB of water
+ `time` (optional): ticks the recipe takes, defaults to 400 (simple) or 300 (advanced)
+ `advanced` (optional): `true` forces the recipe to need the advanced condenser

Elixirs store their effects in the vanilla `minecraft:potion_contents` component:
```js
ServerEvents.recipes(event => {
  const slowness = Item.of('rusticrevived:elixir', '[potion_contents={custom_effects:[{id:"minecraft:slowness",duration:1800}]}]')
  const longSlowness = Item.of('rusticrevived:elixir', '[potion_contents={custom_effects:[{id:"minecraft:slowness",duration:4800}]}]')

  event.recipes.rusticrevived.condenser(slowness, ['minecraft:cobweb', 'minecraft:vine'])
  event.recipes.rusticrevived.condenser(longSlowness, ['minecraft:cobweb', 'minecraft:vine'], 'rusticrevived:horsetail')
  event.recipes.rusticrevived.condenser(slowness, ['minecraft:cobweb', 'minecraft:vine']).advanced(true).time(600)
})
```

## Brewing Barrel ##
`event.recipes.rusticrevived.brewing(result, input, quality?)`
+ `result`: the booze produced (the amount is ignored, the barrel converts the input 1 mB at a time)
+ `input`: the fluid brewed
+ `quality` (optional): `'standard'` (random quality between 5% and 75%) or `'ambrosia'` (between 49% and 74%, cultures never lower the quality)

```js
ServerEvents.recipes(event => {
  event.recipes.rusticrevived.brewing(Fluid.of('rusticrevived:mead', 1000), 'rusticrevived:wildberry_juice')
  event.recipes.rusticrevived.brewing(Fluid.of('rusticrevived:mead', 1000), 'rusticrevived:golden_apple_juice', 'ambrosia')
})
```

## Removing Recipes ##
Every built-in recipe has an id like `rusticrevived:<type>/<name>` (for example `rusticrevived:crushing_tub/grapes` or `rusticrevived:brewing/wine`). They are all listed in JEI with advanced tooltips enabled (F3 + H).
```js
ServerEvents.recipes(event => {
  event.remove({ id: 'rusticrevived:brewing/wine' })
  event.remove({ type: 'rusticrevived:crushing_tub', input: 'minecraft:apple' })
  event.remove({ type: 'rusticrevived:condenser', output: 'rusticrevived:elixir' })
})
```
