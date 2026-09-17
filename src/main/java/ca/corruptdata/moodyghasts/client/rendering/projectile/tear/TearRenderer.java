package ca.corruptdata.moodyghasts.client.rendering.projectile.tear;

import ca.corruptdata.moodyghasts.entity.projectile.tear.TearEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class TearRenderer extends ThrownItemRenderer<TearEntity> {
    public TearRenderer(EntityRendererProvider.Context context) {
        super(context, 2.5F, false);
    }
}
