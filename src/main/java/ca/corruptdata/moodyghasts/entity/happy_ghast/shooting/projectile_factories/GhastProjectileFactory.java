package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.Optional;

public interface GhastProjectileFactory {
    Logger LOGGER = MoodyGhasts.LOGGER;

    /**
     * Creates a projectile entity with mood-scaled properties, tagged with the ghast that
     * fired it (see {@link ModAttachments#OWNING_GHAST}) so it can still be attributed
     * back to that ghast on impact, even if the shooter dismounts mid-flight.
     * Implementations should override {@link #buildProjectile} rather than this method
     * directly, so tagging can't accidentally be skipped.
     *
     * @param level the level where the projectile will be spawned
     * @param owner the player shooting the projectile (controlling the ghast)
     * @param ghast the ghast the player was riding/controlling when the shot was fired
     * @param mood the ghast's current mood value for scaling calculations
     * @param projConfig configuration with base values and scaling functions
     * @return the created projectile entity, tagged and ready to be positioned and launched
     */
    default Projectile createProjectile(Level level, Player owner, HappyGhast ghast, float mood,
                                        ItemPropertyMap.ProjectileConfig projConfig) {
        Projectile projectile = buildProjectile(level, owner, mood, projConfig);
        projectile.setOwner(owner);
        projectile.setData(ModAttachments.OWNING_GHAST, Optional.of(ghast.getUUID()));
        return projectile;
    }

    /**
     * Builds the actual projectile entity with mood-scaled properties.
     * Implementations should calculate values using projConfig methods (e.g., projConfig.getStrength(mood))
     * and include logging with Config.SHOOT_LOGGING.get() check for debugging consistency.
     * Owner does not need to be set here - {@link #createProjectile} sets it on every
     * projectile after this method returns, regardless of what's done here.
     *
     * @param level the level where the projectile will be spawned
     * @param owner the player shooting the projectile (controlling the ghast)
     * @param mood the ghast's current mood value for scaling calculations
     * @param projConfig configuration with base values and scaling functions
     * @return the created projectile entity, not yet tagged or positioned
     */
    Projectile buildProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig);

    SoundEvent getSoundEvent();
}


