package ca.corruptdata.moodyghasts.client.rendering.projectile.ice_charge;

import ca.corruptdata.moodyghasts.entity.projectile.ice_charge.IceChargeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class IceChargeRenderer extends AbstractIceChargeRenderer<IceChargeEntity> {
    public IceChargeRenderer(EntityRendererProvider.Context context) {super(context, 0.45F, -0.175F);}
}