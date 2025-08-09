package ca.corruptdata.moodyghasts.client.rendering;


import net.minecraft.resources.ResourceLocation;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import net.minecraft.util.context.ContextKey;

public class RenderStateKeys {
    public static final ContextKey<Float> MOOD = new ContextKey<>(
            ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "mood"));

    public static final ContextKey<Boolean> IS_CHARGING = new ContextKey<>(
            ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "is_charging"));
}