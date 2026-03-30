package ca.corruptdata.moodyghasts;

import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour.*;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistries {
    // ============================================================
    // Registry Keys
    // ============================================================

    public static final ResourceKey<Registry<GhastProjectileFactory>> PROJECTILE_FACTORIES =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "projectile_factories")
            );

    public static final ResourceKey<Registry<ShootingBehaviourFactory>> SHOOTING_BEHAVIOURS =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "shooting_behaviours")
            );

    // ============================================================
    // Projectile Factories Registry
    // ============================================================

    public static final DeferredRegister<GhastProjectileFactory> PROJECTILE_FACTORY_REGISTER =
            DeferredRegister.create(PROJECTILE_FACTORIES, MoodyGhasts.MOD_ID);

    static {
        PROJECTILE_FACTORY_REGISTER.makeRegistry(builder -> {});

        // Register projectile factories
        PROJECTILE_FACTORY_REGISTER.register("ice_charge", IceChargeFactory::new);
        PROJECTILE_FACTORY_REGISTER.register("large_fireball", LargeFireballFactory::new);
        PROJECTILE_FACTORY_REGISTER.register("wind_charge", WindChargeFactory::new);
        PROJECTILE_FACTORY_REGISTER.register("snow_ball", SnowBallFactory::new);
    }

    // ============================================================
    // Shooting Behaviours Registry
    // ============================================================

    public static final DeferredRegister<ShootingBehaviourFactory> SHOOTING_BEHAVIOUR_REGISTER =
            DeferredRegister.create(SHOOTING_BEHAVIOURS, MoodyGhasts.MOD_ID);

    static {
        SHOOTING_BEHAVIOUR_REGISTER.makeRegistry(builder -> {});

        // Register shooting behaviours
        SHOOTING_BEHAVIOUR_REGISTER.register("single_shot", () -> SingleShotBehaviour::new);
        SHOOTING_BEHAVIOUR_REGISTER.register("barrage", () -> BarrageBehaviour::new);
    }
}