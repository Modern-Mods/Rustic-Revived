Brewing is the process of fermenting liquids. Brewing is done in a brewing barrel.  
All brewing recipes should be visible in JEI.

***

## Brewing Barrel ##
![](http://i.imgur.com/ebZMhQ8.png)  
Brewing Barrels can be made with any wood planks, any wood slabs, and iron. Brewing barrels' GUIs have three tanks, one for input, one for output, and one for culture booze.  
![](http://i.imgur.com/OYXF5zc.png)  
The slot above each tank is for fluid-holding items, like buckets or bottles, which will be used for inputting and outputting through each respective tank. The slot below each tank is where the result from any fluid interactions between the tank and the items in the matching upper slot are returned.  
The middle tank is the input tank, for unfermented fluids. The right-hand tank is the output tank, where fermented fluids appear. The small tank on the left is for culture booze, which is fermented fluid that can significantly reduce the randomness of the quality of the output.

## Usage ##
To use the brewing barrel, simply fill the input tank with a valid fluid. The amount of fluid you insert doesn't matter for the recipe, it simply determines how much fluid will the recipe will return. The brewing process will begin automatically once a valid recipe is entered, and will take half of a Minecraft day to complete. The resulting fluid (assuming it is booze, which by default all recipe outputs are) will have been assigned a random quality between 0.05 and 0.75. Low quality booze will inflict annoying or even harmful debuffs when drank. If you get booze with a quality less than 0.5, it is best to simply discard the booze and try again. Once you've acquired booze of a sufficient quality, you can insert that booze into the culture booze tank of the barrel before making your next batch of the same type of booze. This will greatly reduce the randomness of the output's quality, guaranteeing that the output will have a quality no less than the culture booze's quality - 0.01, and no greater than the culture boozes quality + 0.04. For example, brewing wine from grape juice while wine with a quality of 0.7 is in the culture booze tank will output wine with a quality between 0.69 and 0.74. Culture booze is not consumed in the brewing process.

## Booze ##
Booze can be acquired by brewing various fluids in a brewing barrel. Booze brewed in a brewing barrel should have a quality value between 0 and 1. Drinking booze with a quality less than 0.5 will give terrible results, with the severity of the effects depending on how low the quality was. Drinking booze with a quality of at least 0.5 will give positive results, with the strength of the results dependent on the quality of the booze. Some effects, like absorption from iron wine, may not be very noticeable if you drink booze with a quality just barely above 0.5. In the case of absorption, low qualities increase the number of hearts by values lower than 1, so it can take multiple drinks to get even half a heart. The solution is to just brew better booze.
### Inebriation ###
Drinking any booze has a chance to inebriate you, no matter the quality of the booze. Continuing to drink while inebriated can increase the length and severity of the effect. At levels one and two, inebriation does nothing. At level three, the you will be constantly inflicted with nausea and slowness. At level 4 and above, you will constantly be inflicted with nausea II, slowness II, and blindness. Inebriation cannot be cured by drinking milk. Instead, you can drink water to reduce the length of the effect. Drinking water also has a chance to reduce the severity of the effect.
### Types ###
+ Ale
    - Very saturating, applies the full stomach effect
+ Cider
    - Applies the magic resistance effect
+ Iron Wine
    - Adds absorption hearts to the player with no time limit up to ten extra hearts
+ Mead
    - Applies the wither ward effect
+ Wildberry Wine
    - Increases the amplifier of all positive effects, up to level III
+ Wine
    - Increases the duration of all positive effects up to ten minutes
### Special Effects ###
+ Full Stomach
    - Reduces hunger damage
+ Magic Resistance
    - Reduces magic damage
+ Wither Ward
    - Reduces wither damage