package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public abstract class ShootingBehaviour {
    protected final GhastProjectileFactory factory;
    protected ItemPropertyMap.MoodyProjectile data;
    protected float mood;
    protected int chargeDuration;


    protected ShootingBehaviour(GhastProjectileFactory factory) {
        this.factory = factory;
    }

    public void onChargeStart(HappyGhast ghast, Player player, ItemPropertyMap.MoodyProjectile data, float mood) {
        this.data = data;
        this.chargeDuration = data.shot().chargeDuration();
        this.mood = mood;
        ghast.setData(ModAttachments.IS_CHARGING, true);
        ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, 1);
        ghast.setData(ModAttachments.PROJECTILE_OWNER, player);
        onInitiate(ghast, player); // Hook for additional behavior-specific initialization
    }

    public void tick(HappyGhast ghast) {
        if (!ghast.getData(ModAttachments.IS_CHARGING)) return;

        int chargeTick = ghast.getData(ModAttachments.PROJECTILE_CHARGE_TICK);
        Player player = (Player) ghast.getControllingPassenger();

        if (player == ghast.getData(ModAttachments.PROJECTILE_OWNER) && !(chargeTick > chargeDuration)) {
            ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, chargeTick + 1);

            if (chargeTick == chargeDuration / 2) {
                ghast.level().levelEvent(ghast, 1015, ghast.blockPosition(), 0);
            } else if (chargeTick == chargeDuration) {
                onChargeComplete(ghast, player);
                resetChargeState(ghast);
            }
        } else {
            resetChargeState(ghast);
        }
    }

    private void resetChargeState(HappyGhast ghast) {
        ghast.setData(ModAttachments.IS_CHARGING, false);
        ghast.setData(ModAttachments.CURRENT_PROJECTILE, Items.AIR);
        ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, 0);
    }

    protected Vec3 getProjectileSpawnPos(HappyGhast ghast) {
        Vec3 viewVec = ghast.getViewVector(1.0F);
        return new Vec3(
                ghast.getX() + viewVec.x * 4.0,
                ghast.getEyeY(),
                ghast.getZ() + viewVec.z * 4.0
        );
    }

    protected Vec3 getPlayerAimVector(Player player) {
        float clampedPitch = Mth.clamp(player.getXRot() - 4, -60f, 60f);
        return Vec3.directionFromRotation(clampedPitch, player.getYRot());
    }

    protected void playProjSound(HappyGhast ghast) {
        ghast.level().playSound(null, BlockPos.containing(getProjectileSpawnPos(ghast)),
                factory.getSoundEvent(), SoundSource.NEUTRAL,
                0.5F, 0.4F / (ghast.level().getRandom().nextFloat() * 0.4F + 0.8F));
    }

    protected abstract void onChargeComplete(HappyGhast ghast, Player player);
    public abstract void onInitiate(HappyGhast ghast, Player player);
    public abstract void stop(HappyGhast ghast);

}