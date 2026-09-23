package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Cry extends FiringPattern {

    // Horizontal offset from center to each eye
    private static final double EYE_HORIZONTAL_OFFSET = 0.7;
    // Vertical offset
    private static final double EYE_VERTICAL_OFFSET = 0.5;
    // How far in front of ghast projectiles spawn
    private static final double FORWARD_OFFSET = 3;

    private int totalTears;

    public Cry(ProjectileFactory factory, HappyGhast ghast, Player player,
               ItemPropertyMap.MoodyProjectile data, ItemPropertyMap.MoodContext moodContext) {
        super(factory, ghast, player, data, moodContext);
    }

    @Override
    protected void onChargeComplete() {
        totalTears = data.shot().getCount(moodContext);

        ghast.setData(ModAttachments.IS_FIRING, true);
        ghast.setData(ModAttachments.SHOTS_LEFT, totalTears);
        ghast.setData(ModAttachments.SHOT_DELAY, 0);
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

        handleCrying();
    }

    private void handleCrying() {
        int tearsLeft = ghast.getData(ModAttachments.SHOTS_LEFT);
        int nextDelay = ghast.getData(ModAttachments.SHOT_DELAY);

        if (tearsLeft <= 0) {
            stop();
            return;
        }

        if (nextDelay > 0) {
            ghast.setData(ModAttachments.SHOT_DELAY, nextDelay - 1);
            return;
        }

        shootTear(totalTears - tearsLeft);
        ghast.setData(ModAttachments.SHOTS_LEFT, tearsLeft - 1);
        ghast.setData(ModAttachments.SHOT_DELAY, data.shot().getShotDelay(moodContext));
    }

    private void shootTear(int index) {
        Level level = ghast.level();

        // Perpendicular to the ghast's facing direction, in the horizontal plane.
        Vec3 forward = ghast.getViewVector(1.0F);
        Vec3 rightOffset = new Vec3(-forward.z, 0, forward.x).normalize().scale(EYE_HORIZONTAL_OFFSET);
        Vec3 basePos = new Vec3(
                ghast.getX() + forward.x * FORWARD_OFFSET,
                ghast.getEyeY() - EYE_VERTICAL_OFFSET,
                ghast.getZ() + forward.z * FORWARD_OFFSET
        );
        Vec3 spawnPos = (index % 2 == 0) ? basePos.subtract(rightOffset) : basePos.add(rightOffset);

        float velocity = data.shot().getVelocity(moodContext);
        float inaccuracy = data.shot().getInaccuracy(moodContext);

        Projectile projectile = factory.createProjectile(level, shooter, ghast, moodContext, data.projectile());
        projectile.setPos(spawnPos);
        projectile.shoot(0, -1, 0, velocity, inaccuracy);

        level.addFreshEntity(projectile);
        playProjSound();
        applyMoodDelta();
    }

    @Override
    public void stop() {
        ghast.setData(ModAttachments.IS_FIRING, false);
        ghast.setData(ModAttachments.SHOTS_LEFT, 0);
        ghast.setData(ModAttachments.SHOT_DELAY, 0);

        if(Config.SHOOT_LOGGING.get())
            LOGGER.info("Crying stopped for ghast {}", ghast.getUUID());
    }
}