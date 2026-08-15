package ca.corruptdata.moodyghasts.client;

import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import ca.corruptdata.moodyghasts.client.rendering.happy_ghast.MoodyGhastRenderer;
import ca.corruptdata.moodyghasts.client.rendering.projectile.ice_charge.IceChargeModel;
import ca.corruptdata.moodyghasts.client.rendering.projectile.ice_charge.IceChargeRenderer;
import ca.corruptdata.moodyghasts.client.rendering.projectile.ice_charge.MoodyIceChargeRenderer;
import ca.corruptdata.moodyghasts.client.rendering.projectile.wind_charge.MoodyWindChargeRenderer;
import ca.corruptdata.moodyghasts.entity.ModEntities;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@Mod(value = "moodyghasts", dist = Dist.CLIENT)
public class MoodyGhastsClient {

    public MoodyGhastsClient(IEventBus modEventBus, ModContainer container) {
        // Register the built-in configuration screen
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::registerLayerDefinitions);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerRenderStateModifiers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ICE_CHARGE.get(), IceChargeRenderer::new);
        event.registerEntityRenderer(ModEntities.MOODY_ICE_CHARGE.get(), MoodyIceChargeRenderer::new);
        event.registerEntityRenderer(ModEntities.MOODY_WIND_CHARGE.get(), MoodyWindChargeRenderer::new);
        event.registerEntityRenderer(EntityType.HAPPY_GHAST, MoodyGhastRenderer::new);
    }

    private void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(IceChargeModel.LAYER_LOCATION, IceChargeModel::createBodyLayer);
    }

    private void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                HappyGhastRenderer.class,
                (entity, state) -> state.setRenderData(
                        RenderStateKeys.MOOD, entity.getData(ModAttachments.MOOD)));
        event.registerEntityModifier(
                HappyGhastRenderer.class,
                (entity, state) -> state.setRenderData(
                        RenderStateKeys.IS_CHARGING, entity.getData(ModAttachments.IS_CHARGING)));
        event.registerEntityModifier(
                HappyGhastRenderer.class,
                (entity, state) -> state.setRenderData(
                        RenderStateKeys.IS_FIRING, entity.getData(ModAttachments.IS_FIRING)));
        event.registerEntityModifier(
                HappyGhastRenderer.class,
                (entity, state) -> {
                    GhastMoodMap map = GhastMoodMap.get();
                    int tantrumTick = map != null ? map.getTantrumTick(entity.getData(ModAttachments.MOOD)) : 1;
                    int enragedTicks = entity.getData(ModAttachments.TANTRUM_TICKS);
                    float progress = tantrumTick > 0 ? Mth.clamp((float) enragedTicks / tantrumTick, 0f, 1f) : 0f;
                    state.setRenderData(RenderStateKeys.TANTRUM_PROGRESS, progress);
                });
    }
}