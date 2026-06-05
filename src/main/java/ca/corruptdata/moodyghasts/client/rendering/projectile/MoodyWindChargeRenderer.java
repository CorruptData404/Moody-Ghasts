package ca.corruptdata.moodyghasts.client.rendering.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class MoodyWindChargeRenderer extends WindChargeRenderer {
    public MoodyWindChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = 3.0F;
        poseStack.scale(scale, scale, scale);
        super.render(state, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}