package ca.corruptdata.moodyghasts.client;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import ca.corruptdata.moodyghasts.client.rendering.happy_ghast.MoodyGhastRenderer;
import ca.corruptdata.moodyghasts.client.rendering.projectile.IceChargeRenderer;
import ca.corruptdata.moodyghasts.client.rendering.projectile.MoodyIceChargeRenderer;
import ca.corruptdata.moodyghasts.client.rendering.projectile.MoodyWindChargeRenderer;
import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
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

        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerRenderStateModifiers);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ICE_CHARGE.get(), IceChargeRenderer::new);
        event.registerEntityRenderer(ModEntities.MOODY_ICE_CHARGE.get(), MoodyIceChargeRenderer::new);
        event.registerEntityRenderer(ModEntities.MOODY_WIND_CHARGE.get(), MoodyWindChargeRenderer::new);
        event.registerEntityRenderer(EntityType.HAPPY_GHAST, MoodyGhastRenderer::new);
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
                        RenderStateKeys.IS_BARRAGING, entity.getData(ModAttachments.IS_BARRAGING)));
    }
}