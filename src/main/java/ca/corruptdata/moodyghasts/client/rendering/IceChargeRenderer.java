package ca.corruptdata.moodyghasts.client.rendering;

import ca.corruptdata.moodyghasts.entity.projectile.AbstractIceChargeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class IceChargeRenderer extends ThrownItemRenderer<AbstractIceChargeEntity> {
    public IceChargeRenderer(EntityRendererProvider.Context context) {
        super(context, 2.0F, false);
    }
}