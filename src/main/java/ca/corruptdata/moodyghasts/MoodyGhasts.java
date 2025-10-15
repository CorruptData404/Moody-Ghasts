package ca.corruptdata.moodyghasts;

import ca.corruptdata.moodyghasts.client.rendering.IceChargeRenderer;
import ca.corruptdata.moodyghasts.client.rendering.MoodyWindChargeRenderer;
import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import ca.corruptdata.moodyghasts.client.rendering.happyghast.MoodGhastRenderer;
import ca.corruptdata.moodyghasts.component.ModDataComponentTypes;
import ca.corruptdata.moodyghasts.entity.HappyGhastHandler;
import ca.corruptdata.moodyghasts.entity.ModEntities;
import ca.corruptdata.moodyghasts.item.ModItems;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholds;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholdsManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;


@Mod(MoodyGhasts.MOD_ID)
public class MoodyGhasts {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "moodyghasts";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MoodyGhasts(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading

        ModDataComponentTypes.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        NeoForge.EVENT_BUS.register(new HappyGhastHandler());


        modEventBus.addListener(this::commonSetup);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        modEventBus.addListener(this::registerDataMaps);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModDispenserBehaviors::register);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES || event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.ICE_CHARGE);
        }
        if(event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.SPICY_COOKIE);
            event.accept(ModItems.FROSTED_COOKIE);
        }
    }

    private void registerDataMaps(RegisterDataMapTypesEvent event) {
        event.register(DataMapType.builder(MoodThresholds.ID, MoodThresholdsManager.REGISTRY_KEY, MoodThresholds.CODEC)
                .synced(MoodThresholds.CODEC, true)
                .build());
    }

//TODO: This crashes. Need to Figure out how/when to call updateThresholds properly

//    @EventBusSubscriber(modid = MoodyGhasts.MOD_ID)
//    public static class ServerEvents {
//
//        @SubscribeEvent
//        public static void onServerStarted(ServerStartedEvent event) {
//            MoodThresholdsManager.updateThresholds(event.getServer().registryAccess());
//        }
//    }
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Client setup code here
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.ICE_CHARGE.get(), IceChargeRenderer::new);
            event.registerEntityRenderer(ModEntities.MOODY_ICE_CHARGE.get(), IceChargeRenderer::new);
            event.registerEntityRenderer(ModEntities.MOODY_WIND_CHARGE.get(), MoodyWindChargeRenderer::new);
            event.registerEntityRenderer(EntityType.HAPPY_GHAST, MoodGhastRenderer::new);

        }

        @SubscribeEvent
        public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
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
                            RenderStateKeys.IS_SNOWBALL_BARRAGE, entity.getData(ModAttachments.IS_SNOWBALL_BARRAGE)));
        }
    }
}