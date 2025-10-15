package ca.corruptdata.moodyghasts.mixins;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(Gui.ContextualInfo.class)
public abstract class ContextualInfoMixin {

    @Mutable
    @Shadow
    @Final
    private static Gui.ContextualInfo[] $VALUES;

    @Invoker("<init>")
    public static Gui.ContextualInfo moodyghasts$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static void moodyghasts$add(String internalName) {
        Gui.ContextualInfo[] newValues = new Gui.ContextualInfo[$VALUES.length + 1];
        System.arraycopy($VALUES, 0, newValues, 0, $VALUES.length);
        Gui.ContextualInfo contextualInfo = moodyghasts$invokeInit(internalName, $VALUES[$VALUES.length - 1].ordinal() + 1);
        newValues[$VALUES.length] = contextualInfo;
        $VALUES = newValues;
    }

    static {
        moodyghasts$add("GHAST_MOOD_BAR");
    }
}