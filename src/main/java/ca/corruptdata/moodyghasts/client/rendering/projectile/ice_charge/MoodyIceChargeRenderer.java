package ca.corruptdata.moodyghasts.client.rendering.projectile.ice_charge;

import ca.corruptdata.moodyghasts.entity.projectile.ice_charge.MoodyIceChargeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MoodyIceChargeRenderer extends AbstractIceChargeRenderer<MoodyIceChargeEntity> {
    public MoodyIceChargeRenderer(EntityRendererProvider.Context context) {super(context, 1.5F, -0.65F); }
}