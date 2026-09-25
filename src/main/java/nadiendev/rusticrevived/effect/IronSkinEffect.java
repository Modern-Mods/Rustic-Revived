package nadiendev.rusticrevived.effect;

import nadiendev.rusticrevived.RusticRevived;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Iron Skin: +3 armor and +2 armor toughness per level. The visual layer is added client side.
 */
public class IronSkinEffect extends RusticEffect {
	public IronSkinEffect() {
		super(MobEffectCategory.BENEFICIAL, 16777148);
		addAttributeModifier(Attributes.ARMOR, RusticRevived.id("effect.iron_skin.armor"), 3D, AttributeModifier.Operation.ADD_VALUE);
		addAttributeModifier(Attributes.ARMOR_TOUGHNESS, RusticRevived.id("effect.iron_skin.toughness"), 2D, AttributeModifier.Operation.ADD_VALUE);
	}
}
