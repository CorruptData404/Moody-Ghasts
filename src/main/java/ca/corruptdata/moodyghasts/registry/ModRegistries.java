package ca.corruptdata.moodyghasts.registry;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern_factories.*;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistries {
    // ============================================================
    // Registry Keys
    // ============================================================

    public static final ResourceKey<Registry<ProjectileFactory>> PROJECTILE_FACTORIES =
            ResourceKey.createRegistryKey(
                    Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "projectile_factories")
            );

    public static final ResourceKey<Registry<FiringPatternFactory>> FIRING_PATTERN_FACTORIES =
            ResourceKey.createRegistryKey(
                    Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "firing_pattern_factories")
            );

    // ============================================================
    // Projectile Factories Registry
    // ============================================================

    public static final DeferredRegister<ProjectileFactory> PROJECTILE_FACTORY_REGISTER =
            DeferredRegister.create(PROJECTILE_FACTORIES, MoodyGhasts.MOD_ID);

    static {
        PROJECTILE_FACTORY_REGISTER.makeRegistry(builder -> {});

        // Register projectile factories
        PROJECTILE_FACTORY_REGISTER.register("ice_charge", IceChargeFactory::new);
        PROJECTILE_FACTORY_REGISTER.register("wind_charge", WindChargeFactory::new);
        PROJECTILE_FACTORY_REGISTER.register("large_fireball", LargeFireballFactory::new);
        PROJECTILE_FACTORY_REGISTER.register("dragon_fireball", DragonFireballFactory::new);
        PROJECTILE_FACTORY_REGISTER.register("snow_ball", SnowBallFactory::new);
    }

    // ============================================================
    // Firing Pattern Factories Registry
    // ============================================================

    public static final DeferredRegister<FiringPatternFactory> FIRING_PATTERN_FACTORY_REGISTER =
            DeferredRegister.create(FIRING_PATTERN_FACTORIES, MoodyGhasts.MOD_ID);

    static {
        FIRING_PATTERN_FACTORY_REGISTER.makeRegistry(builder -> {});

        // Register shooting behaviours
        FIRING_PATTERN_FACTORY_REGISTER.register("single_shot", SingleShotFactory::new);
        FIRING_PATTERN_FACTORY_REGISTER.register("barrage", BarrageFactory::new);
    }
}