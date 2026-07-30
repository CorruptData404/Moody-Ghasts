package ca.corruptdata.moodyghasts.client.rendering.happy_ghast;

import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.ghast.HappyGhastModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;

public class GhastTantrumOverlayLayer extends RenderLayer<HappyGhastRenderState, HappyGhastModel> {
    private final MoodyGhastRenderer moodyRenderer;

    public GhastTantrumOverlayLayer(MoodyGhastRenderer parent) {
        super(parent);
        this.moodyRenderer = parent;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, HappyGhastRenderState state, float yRot, float xRot) {
        float progress = state.getRenderDataOrDefault(RenderStateKeys.TANTRUM_PROGRESS, 0f);
        if (progress <= 0f) return;

        int green_blue = (int) (0xFF * (1f - progress)); // fades toward 0 as progress -> 1
        int tint = 0xFF000000 | 0xFF0000 | (green_blue << 8) | green_blue; // white -> red

        renderColoredCutoutModel(getParentModel(), moodyRenderer.getTextureLocation(state), poseStack, collector, lightCoords, state, tint, 1);
    }
}