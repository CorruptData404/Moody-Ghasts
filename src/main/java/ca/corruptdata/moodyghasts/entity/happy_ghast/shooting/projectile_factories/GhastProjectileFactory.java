package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public interface GhastProjectileFactory {
    Logger LOGGER = MoodyGhasts.LOGGER;

    /**
     * Creates a projectile entity with mood-scaled properties.
     * Implementations should calculate values using projConfig methods (e.g., projConfig.getStrength(mood))
     * and include logging with Config.SHOOT_LOGGING.get() check for debugging consistency.
     *
     * @param level the level where the projectile will be spawned
     * @param owner the player shooting the projectile (controlling the ghast).
     *              If the projectile constructor doesn't accept an owner parameter,
     *              set it manually using projectile.setOwner(owner).
     * @param mood the ghast's current mood value for scaling calculations
     * @param projConfig configuration with base values and scaling functions
     * @return the created projectile entity ready to be positioned and launched
     */
    Projectile createProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig);

    SoundEvent getSoundEvent();
}

