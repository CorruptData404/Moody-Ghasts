package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour;

import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;

@FunctionalInterface
public interface ShootingBehaviourFactory {
    ShootingBehaviour create(GhastProjectileFactory projectileFactory);
}
