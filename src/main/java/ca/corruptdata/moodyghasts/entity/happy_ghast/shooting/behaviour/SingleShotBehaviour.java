package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour;

import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastMoodHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SingleShotBehaviour extends ShootingBehaviour {
    public SingleShotBehaviour(GhastProjectileFactory factory) {
        super(factory);
    }

    @Override
    protected void onChargeComplete(HappyGhast ghast, Player player) {
        Level level = ghast.level();

        // Create and configure projectile
        Projectile projectile = factory.createProjectile(
                level, player, mood, data.projectile()
        );

        Vec3 direction = getPlayerAimVector(player);

        projectile.setPos(getProjectileSpawnPos(ghast));
        projectile.shoot(
                direction.x, direction.y, direction.z,
                data.shot().getVelocity(mood),
                data.shot().getInaccuracy(mood));

        // Spawn projectile
        level.levelEvent(null, 1016, ghast.blockPosition(), 0);
        level.addFreshEntity(projectile);
        playProjSound(ghast);
        GhastMoodHandler.adjustMood(ghast, data.moodDelta());
    }

    @Override
    public void onInitiate(HappyGhast ghast, Player player) {

    }

    @Override
    public void stop(HappyGhast ghast) {

    }
}
