package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class FiringPattern {
    protected final ProjectileFactory factory;
    protected final ItemPropertyMap.MoodyProjectile data;
    protected final Player shooter;
    protected final HappyGhast ghast;
    protected final float mood;
    protected final int chargeDuration;
    protected static final Logger LOGGER = MoodyGhasts.LOGGER;


    protected FiringPattern(ProjectileFactory factory, HappyGhast ghast, Player player, ItemPropertyMap.MoodyProjectile data, float mood) {
        this.factory = factory;
        this.data = data;
        this.chargeDuration = data.shot().chargeDuration();
        this.mood = mood;
        this.shooter = player;
        this.ghast = ghast;
        ghast.setData(ModAttachments.IS_CHARGING, true);
        ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, 1);

        if(Config.SHOOT_LOGGING.get())
            LOGGER.info("Starting {} tick charge for ghast {}", chargeDuration, ghast.getUUID());
    }

    public void tick() {
        if (!ghast.getData(ModAttachments.IS_CHARGING)) return;

        int chargeTick = ghast.getData(ModAttachments.PROJECTILE_CHARGE_TICK);
        Player currentRider = ghast.getControllingPassenger() instanceof Player p ? p : null;

        // Cancel charge if the current rider is different from the one that initiated the behaviour
        if (currentRider == shooter && chargeTick <= chargeDuration) {
            ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, chargeTick + 1);

            if (chargeTick == chargeDuration / 2) {
                ghast.level().levelEvent(ghast, 1015, ghast.blockPosition(), 0);
            } else if (chargeTick == chargeDuration) {

                if(Config.SHOOT_LOGGING.get())
                    LOGGER.info("Completed {} tick charge for ghast {}", chargeDuration, ghast.getUUID());

                onChargeComplete();
                resetChargeState();
            }
        } else {
            resetChargeState();
        }
    }

    private void resetChargeState() {
        ghast.setData(ModAttachments.IS_CHARGING, false);
        ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, 0);

        if(Config.SHOOT_LOGGING.get())
            LOGGER.info("Resetting charge state for ghast {}", ghast.getUUID());
    }

    protected Vec3 getProjectileSpawnPos() {
        Vec3 viewVec = ghast.getViewVector(1.0F);
        return new Vec3(
                ghast.getX() + viewVec.x * 4.0,
                ghast.getEyeY(),
                ghast.getZ() + viewVec.z * 4.0
        );
    }

    protected Vec3 getShooterAimVector() {
        float clampedPitch = Mth.clamp(shooter.getXRot() - 4, -60f, 60f);
        return Vec3.directionFromRotation(clampedPitch, shooter.getYRot());
    }

    protected void playProjSound() {
        ghast.level().playSound(null, BlockPos.containing(getProjectileSpawnPos()),
                factory.getSoundEvent(), SoundSource.NEUTRAL,
                0.35F, 0.4F / (ghast.level().getRandom().nextFloat() * 0.4F + 0.8F));
    }

    protected abstract void onChargeComplete();
    public abstract void stop();

}