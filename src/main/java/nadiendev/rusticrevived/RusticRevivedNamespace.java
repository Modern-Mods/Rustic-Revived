package nadiendev.rusticrevived;

import net.neoforged.fml.common.Mod;

/**
 * Empty mod entry for the {@value RusticRevived#NAMESPACE} namespace. Items report their
 * namespace as creator mod, so declaring it as a mod makes tooltips show "Rustic Revived" and lets
 * JEI find everything with both {@code @rustic} and {@code @rusticrevived}.
 */
@Mod(RusticRevived.NAMESPACE)
public final class RusticRevivedNamespace {
	public RusticRevivedNamespace() {
	}
}
