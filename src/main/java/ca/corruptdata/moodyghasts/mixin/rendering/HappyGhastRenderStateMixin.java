package ca.corruptdata.moodyghasts.mixin.rendering;

import ca.corruptdata.moodyghasts.api.HappyGhastRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HappyGhastRenderState.class)
public class HappyGhastRenderStateMixin implements HappyGhastRenderStateAccessor {
    @Unique
    public boolean moodyghasts$isShooting = false;
    @Unique
    public float moodyghasts$mood = 0.6F;

    @Override
    public boolean isMoodyghasts$isShooting() {
        return moodyghasts$isShooting;
    }

    @Override
    public float getMoodyghasts$mood() {
        return moodyghasts$mood;
    }

    @Override
    public void setMoodyghasts$isShooting(boolean moodyghasts$isShooting) {
        this.moodyghasts$isShooting = moodyghasts$isShooting;
    }

    @Override
    public void setMoodyghasts$mood(float moodyghasts$mood) {
        this.moodyghasts$mood = moodyghasts$mood;
    }
}