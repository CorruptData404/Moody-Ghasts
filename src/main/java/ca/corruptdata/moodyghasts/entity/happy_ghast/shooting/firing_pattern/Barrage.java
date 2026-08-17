package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastMoodHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Barrage extends FiringPattern {

    private int totalProjectiles;

    public Barrage(ProjectileFactory factory, HappyGhast ghast,
                   Player player, ItemPropertyMap.MoodyProjectile data, float mood) {
        super(factory, ghast, player, data, mood);
    }

    @Override
    protected void onChargeComplete() {
        totalProjectiles = data.shot().getCount(mood);

        ghast.setData(ModAttachments.IS_FIRING, true);
        ghast.setData(ModAttachments.SHOTS_LEFT, totalProjectiles);
        ghast.setData(ModAttachments.BARRAGE_DELAY, 0);
    }

    @Override
    public void tick() {
        if (ghast.getData(ModAttachments.IS_CHARGING)) {
            super.tick();
            return;
        }

        if (!ghast.getData(ModAttachments.IS_FIRING)) return;

        if (shooter != ghast.getControllingPassenger()) {
            stop();
            return;
        }

        handleBarrage();
    }

    private void handleBarrage() {
        int projectilesLeft = ghast.getData(ModAttachments.SHOTS_LEFT);
        int nextDelay = ghast.getData(ModAttachments.BARRAGE_DELAY);

        if (projectilesLeft <= 0) {
            stop();
            return;
        }

        float progress = (float) projectilesLeft / totalProjectiles;

        // Calculate logarithmic delay (increases as progress decreases)
        // Maps progress from 1.0->0.0 to 0->5 logarithmically
        float delayFactor = -2.0f * (float)Math.log(progress + 0.1f);
        int delay = Math.clamp((int) delayFactor, 0, 5);

        if (nextDelay > 0) {
            ghast.setData(ModAttachments.BARRAGE_DELAY, nextDelay - 1);
            return;
        }

        shootProjectile(progress);
        ghast.setData(ModAttachments.SHOTS_LEFT, projectilesLeft - 1);
        ghast.setData(ModAttachments.BARRAGE_DELAY, delay);
    }

    private void shootProjectile(float progress) {
        Level level = ghast.level();
        Vec3 spawnPos = getProjectileSpawnPos();
        Vec3 direction = getShooterAimVector();
        float inaccuracy = data.shot().getInaccuracy(mood);

        RandomSource random = ghast.getRandom();
        direction = new Vec3(
                random.triangle(direction.x, inaccuracy),
                random.triangle(direction.y, inaccuracy),
                random.triangle(direction.z, inaccuracy)
        ).normalize();

        float progressScale = 0.5f + (0.9f * (float)Math.log10(progress * 9 + 1));
        float speedFactor = data.shot().getVelocity(mood) * progressScale;

        if(Config.SHOOT_LOGGING.get())
            LOGGER.info("""
                    New Barrage Projectile with:
                    Speed Factor: {}
                    Inaccuracy: {}""", speedFactor, inaccuracy);

        Projectile projectile = factory.createProjectile(level, shooter, ghast, mood, data.projectile());
        projectile.setPos(spawnPos);
        projectile.shoot(
                direction.x, direction.y, direction.z,
                speedFactor, 0 // pass 0 inaccuracy since handled it above
        );

        level.addFreshEntity(projectile);
        playProjSound();
        GhastMoodHandler.adjustMood(ghast, data.moodDelta());
    }

    @Override
    public void stop() {
        ghast.setData(ModAttachments.IS_FIRING, false);
        ghast.setData(ModAttachments.SHOTS_LEFT, 0);
        ghast.setData(ModAttachments.BARRAGE_DELAY, 0);

        if(Config.SHOOT_LOGGING.get())
            LOGGER.info("Barrage stopped for ghast {}", ghast.getUUID());
    }
}