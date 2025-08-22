package ca.corruptdata.moodyghasts.client.rendering;

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
        float scale = 2.0F; // Adjust this value to change the size
        poseStack.scale(scale, scale, scale);
        super.render(state, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}