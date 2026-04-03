package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour;

import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastMoodHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SingleShotBehaviour extends ShootingBehaviour {

    public SingleShotBehaviour(GhastProjectileFactory factory, HappyGhast ghast,
                            Player player, ItemPropertyMap.MoodyProjectile data, float mood) {
        super(factory, ghast, player, data, mood);
    }

    @Override
    protected void onChargeComplete() {
        Level level = ghast.level();

        // Create and configure projectile
        Projectile projectile = factory.createProjectile(
                level, shooter, mood, data.projectile()
        );

        Vec3 direction = getShooterAimVector();

        projectile.setPos(getProjectileSpawnPos());
        projectile.shoot(
                direction.x, direction.y, direction.z,
                data.shot().getVelocity(mood),
                data.shot().getInaccuracy(mood));

        // Spawn projectile
        level.levelEvent(null, 1016, ghast.blockPosition(), 0);
        level.addFreshEntity(projectile);
        playProjSound();
        GhastMoodHandler.adjustMood(ghast, data.moodDelta());
    }


    @Override
    public void stop() {

    }
}
