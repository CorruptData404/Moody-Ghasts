package ca.corruptdata.moodyghasts.client.rendering.projectile;

import ca.corruptdata.moodyghasts.entity.projectile.IceChargeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class IceChargeRenderer extends ThrownItemRenderer<IceChargeEntity> {
    public IceChargeRenderer(EntityRendererProvider.Context context) {super(context, 1.0F, false);}
}