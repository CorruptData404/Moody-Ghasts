package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour;

import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface ShootingBehaviourFactory {
    ShootingBehaviour create(GhastProjectileFactory factory, HappyGhast ghast,
                             Player player, ItemPropertyMap.MoodyProjectile data, float mood);
}
