package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public interface GhastProjectileFactory {
    Projectile createProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig);

    SoundEvent getSoundEvent();
}

