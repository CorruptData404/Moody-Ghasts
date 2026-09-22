package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern;

import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SingleShot extends FiringPattern {

    public SingleShot(ProjectileFactory factory, HappyGhast ghast, Player player,
                      ItemPropertyMap.MoodyProjectile data, ItemPropertyMap.MoodContext moodContext) {
        super(factory, ghast, player, data, moodContext);
    }

    @Override
    protected void onChargeComplete() {
        //Firing is instant upon charge completion so updating ModAttachments.IS_FIRING is unnecessary

        Level level = ghast.level();

        Projectile projectile = factory.createProjectile(
                level, shooter, ghast, moodContext, data.projectile()
        );

        Vec3 direction = getShooterAimVector();

        projectile.setPos(getProjectileSpawnPos());
        projectile.shoot(
                direction.x, direction.y, direction.z,
                data.shot().getVelocity(moodContext),
                data.shot().getInaccuracy(moodContext));

        // Spawn projectile
        level.levelEvent(null, 1016, ghast.blockPosition(), 0);
        level.addFreshEntity(projectile);
        playProjSound();
        applyMoodDelta();
    }


    @Override
    public void stop() {

    }
}