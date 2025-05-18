package ca.corruptdata.moodyghasts.client.renderer;

import ca.corruptdata.moodyghasts.entity.projectile.AbstractIceCharge;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class IceChargeRenderer extends ThrownItemRenderer<AbstractIceCharge> {
    public IceChargeRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true); // 1.0F is scale, true for full bright
    }
}