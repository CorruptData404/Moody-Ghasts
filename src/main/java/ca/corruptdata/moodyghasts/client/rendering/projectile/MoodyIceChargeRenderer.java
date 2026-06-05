package ca.corruptdata.moodyghasts.client.rendering.projectile;

import ca.corruptdata.moodyghasts.entity.projectile.MoodyIceChargeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class MoodyIceChargeRenderer extends ThrownItemRenderer<MoodyIceChargeEntity> {
    public MoodyIceChargeRenderer(EntityRendererProvider.Context context) {super(context, 3.0F, false);}
}