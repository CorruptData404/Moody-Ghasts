package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;

public class PlayerIceChargeEntity extends AbstractIceChargeEntity {
    private static final int PLAYER_EFFECT_RADIUS = 3;
    private static final int DAMAGE = 3;

    // Factory constructor for registration
    public PlayerIceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, Level world) {
        super(type, world);
    }

    // Player-thrown constructor
    public PlayerIceChargeEntity(LivingEntity shooter, Level level) {
        super(ModEntities.PLAYER_ICE_CHARGE.get(), shooter, level);
    }

    // Movement-specific constructor
    public PlayerIceChargeEntity(Level level, double x, double y, double z, Vec3 movement) {
        super(ModEntities.PLAYER_ICE_CHARGE.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(movement);
    }

    @Override
    protected int getEffectRadius() {
        return PLAYER_EFFECT_RADIUS;
    }
    @Override
    protected int getDamage() {
        return DAMAGE;
    }

}
