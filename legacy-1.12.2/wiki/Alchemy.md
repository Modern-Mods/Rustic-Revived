Alchemy is the process of brewing elixirs in condensers.  
All recipes should be visible in JEI.  
In the 1.11 versions of the mod, [CraftTweaker support](https://github.com/the-realest-stu/Rustic/wiki/CraftTweaker-Support) is included so modpack makers can add or remove alchemy recipes.

***

## Basic Alchemy ##
![](http://i.imgur.com/tphAE0W.png)  
Basic Alchemy is performed in a condenser. In order to operate, a condenser must have retorts on either side, both facing towards the condenser, so that the retorts' tubes are attached to the condenser. Condensers can be made with bricks, white stained clay, and a bucket. Retorts can be made with bricks, iron, and a bucket.  
![](http://i.imgur.com/DspCBNY.png)  
The tank on the right side of the GUI is for water. The tank can be filled or emptied by right-clicking the block with buckets in hand, or by shift-right-clicking the block with an empty hand to void all contained fluid. The slot on the bottom middle of the GUI is for any fuel item that works in a furnace. The slot on the top right of the GUI is for glass bottles. The large slot below that is the output slot, where finished elixers appear. The two slots on the left of the GUI are for ingredients. Brewing is done by putting valid ingredients into the ingredient slots, and supplying the condenser with enough fuel, water, and bottles to brew.

## Advanced Alchemy ##
![](http://i.imgur.com/3kXM2Q7.png)  
Advanced Alchemy is performed in an advanced condenser. In order to operate, an advanced condenser must have advanced retorts on all three sides other than the front, all facing towards the advanced condenser, so that the retorts' tubes are attached to the condenser. Condensers can be made with nether bricks, an iron block, and a bucket. Retorts can be made with nether bricks, iron, and a bucket.  
![](http://i.imgur.com/fs6I8Q1.png)  
The tank on the right side of the GUI is for water. The tank can be filled or emptied by right-clicking the block with buckets in hand, or by shift-right-clicking the block with an empty hand to void all contained fluid. The slot on the bottom middle of the GUI is for any fuel item that works in a furnace. The slot on the top right of the GUI is for glass bottles. The large slot below that is the output slot, where finished elixers appear. The slot on the top middle of the GUI is for modifiers. The three slots on the left of the GUI are for ingredients. Brewing is done by putting valid ingredients into the ingredient slots, optionally putting a valid modifier into the modifier slot, and supplying the advanced condenser with enough fuel, water, and bottles to brew.

## Elixirs ##
Elixirs are like potions, but they are stackable up to 16. They also cannot currently be turned into splash potions, lingering potions, or tipped arrows.
### Types ###
+ Elixir of Instant Health
+ Elixir of Regeneration
+ Elixir of Wither
+ Elixir of Night Vision
+ Elixir of Speed
+ Elixir of Fire Resistance
+ Elixir of Health Boost
+ Elixir of Haste
+ Elixir of Strength
+ Elixir of Iron Skin
+ Elixir of Feather
+ Elixir of Blazing Trail

## Special Effects ##
+ Iron Skin
    - Adds armor and armor toughness
    - Gives an iron overlay to the model of affected entities
+ Feather
    - Slows the falling speed of affected entities
    - Negates fall damage
+ Blazing Trail
    - Affected entities leave a trail of fire behind them
    

## Automating Condensers ##
Condensers inventory, both basic and advanced, are sided, just like vanilla blocks. This means the slot you can insert/extract from depends on the side with which you interact with the condenser.

Retorts do use a side and so prevent interaction from that side, unless you have some sort of remote interaction. Advanced condenser inventory can be accessed with the top block, while only the standard condenser's bottom block can be used for automation.

The sides behave like so:
+ **top** (Advanced Condenser only): modifier slot
+ **back**: bottle slot and fuel slots (*Note that items that can be used as fuel in a furnace will *never* go in the bottle slot. You can still place them here yourself using the GUI*)
+ **side** and **front**: ingredients slots
+ **bottom**: output slot (only extracting is possible, of course)

Fluid can be inserted from all faces using pipes.