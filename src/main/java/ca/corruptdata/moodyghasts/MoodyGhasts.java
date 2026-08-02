package ca.corruptdata.moodyghasts;

import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastInteractionHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastMoodHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastMovementHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastShootingHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import ca.corruptdata.moodyghasts.entity.ModEntities;
import ca.corruptdata.moodyghasts.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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

        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);

        GhastShootingHandler shootingHandler = new GhastShootingHandler();
        GhastInteractionHandler interactionHandler = new GhastInteractionHandler(shootingHandler);

        NeoForge.EVENT_BUS.register(shootingHandler);
        NeoForge.EVENT_BUS.register(interactionHandler);
        NeoForge.EVENT_BUS.register(new GhastMoodHandler());
        NeoForge.EVENT_BUS.register(new GhastMovementHandler());

        ModRegistries.PROJECTILE_FACTORY_REGISTER.register(modEventBus);
        ModRegistries.SHOOTING_BEHAVIOUR_REGISTER.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
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
        event.register(GhastMoodMap.DATA_MAP);
        event.register(ItemPropertyMap.MoodyConsumable.DATA_MAP);
        event.register(ItemPropertyMap.MoodyProjectile.DATA_MAP);
    }
}