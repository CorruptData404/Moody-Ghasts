package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.entity.happy_ghast.GhastMoodHandler;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BarrageBehaviour extends ShootingBehaviour {

    public BarrageBehaviour(GhastProjectileFactory factory) {
        super(factory);
    }

    @Override
    protected void onChargeComplete(HappyGhast ghast, Player player) {
        int totalProjectiles = data.shot().getCount(mood);
        ghast.setData(ModAttachments.IS_BARRAGING, true);
        ghast.setData(ModAttachments.SHOTS_LEFT, totalProjectiles);
        ghast.setData(ModAttachments.BARRAGE_DELAY, 0);
    }

    @Override
    public void onInitiate(HappyGhast ghast, Player player) {

    }

    @Override
    public void tick(HappyGhast ghast) {
        if (ghast.getData(ModAttachments.IS_CHARGING)) {
            super.tick(ghast);
            return;
        }

        if (!ghast.getData(ModAttachments.IS_BARRAGING)) return;

        Player player = ghast.getControllingPassenger() instanceof Player p ? p : null;
        if (player == null) {
            stop(ghast);
            return;
        }

        handleBarrage(ghast, player);
    }

    private void handleBarrage(HappyGhast ghast, Player player) {
        int projectilesLeft = ghast.getData(ModAttachments.SHOTS_LEFT);
        int nextDelay = ghast.getData(ModAttachments.BARRAGE_DELAY);

        if (projectilesLeft <= 0) {
            stop(ghast);
            return;
        }

        float progress = (float) projectilesLeft / data.shot().getCount(mood);

        // Calculate logarithmic delay (increases as progress decreases)
        // Maps progress from 1.0->0.0 to 0->5 logarithmically
        float delayFactor = -2.0f * (float)Math.log(progress + 0.1f);
        int delay = Math.max(0, Math.min(5, (int)delayFactor));

        if (nextDelay > 0) {
            ghast.setData(ModAttachments.BARRAGE_DELAY, nextDelay - 1);
            return;
        }

        shootProjectile(ghast, player, progress);
        ghast.setData(ModAttachments.SHOTS_LEFT, projectilesLeft - 1);
        ghast.setData(ModAttachments.BARRAGE_DELAY, delay);
    }

    private void shootProjectile(HappyGhast ghast, Player player, float progress) {
        Level level = ghast.level();
        Vec3 spawnPos = getProjectileSpawnPos(ghast);
        Vec3 direction = getPlayerAimVector(player);
        float speedFactor = data.shot().getVelocity(mood) * (0.8f + (0.7f * (float)Math.log10(progress + 0.1f) + 0.7f));
        float inaccuracy = data.shot().getInaccuracy(mood);

        Projectile projectile = factory.createProjectile(level, player, mood, data.projectile());
        projectile.setPos(spawnPos);
        projectile.shoot(
                direction.x, direction.y, direction.z,
                speedFactor, inaccuracy
        );

        level.addFreshEntity(projectile);
        playProjSound(ghast);
        GhastMoodHandler.adjustMood(ghast, data.moodDelta());
    }

    @Override
    public void stop(HappyGhast ghast) {
        ghast.setData(ModAttachments.IS_BARRAGING, false);
        ghast.setData(ModAttachments.SHOTS_LEFT, 0);
        ghast.setData(ModAttachments.BARRAGE_DELAY, 0);
    }
}
