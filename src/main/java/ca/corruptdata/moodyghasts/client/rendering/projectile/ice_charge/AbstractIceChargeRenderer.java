package ca.corruptdata.moodyghasts.client.rendering.projectile.ice_charge;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.projectile.ice_charge.AbstractIceChargeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public abstract class AbstractIceChargeRenderer<T extends AbstractIceChargeEntity> extends EntityRenderer<T, IceChargeRenderState> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "textures/entity/projectile/ice_charge.png");
    private final IceChargeModel model;
    private final float scale;
    private final float yTranslate;

    protected AbstractIceChargeRenderer(EntityRendererProvider.Context context, float scale, float yTranslate) {
        super(context);
        this.model = new IceChargeModel(context.bakeLayer(IceChargeModel.LAYER_LOCATION));
        this.scale = scale;
        this.yTranslate = yTranslate;
    }
    
    @Override
    public IceChargeRenderState createRenderState() {
        return new IceChargeRenderState();
    }

    @Override
    public void extractRenderState(T entity, IceChargeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.travelYRot = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
    }

    @Override
    public void submit(IceChargeRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        super.submit(renderState, poseStack, collector, cameraState);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-renderState.travelYRot));
        poseStack.translate(0.0F, yTranslate, 0.0F);
        poseStack.scale(scale, scale, scale);

        model.setupAnim(renderState);

        collector.submitModelPart(model.getBody(), poseStack, RenderTypes.entityTranslucent(TEXTURE), renderState.lightCoords, OverlayTexture.NO_OVERLAY, null);
        collector.submitModelPart(model.getDecal(), poseStack, RenderTypes.entityCutout(TEXTURE), renderState.lightCoords, OverlayTexture.NO_OVERLAY, null);

        poseStack.popPose();
    }
}