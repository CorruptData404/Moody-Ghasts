package ca.corruptdata.moodyghasts.client.rendering.projectile.wind_charge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class MoodyWindChargeRenderer extends WindChargeRenderer {
    public MoodyWindChargeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        float scale = 3.5F;
        poseStack.translate(0.0F, 0.35F, 0.0F);
        poseStack.scale(scale, scale, scale);
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
    }
}