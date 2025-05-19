package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;

public class PlayerIceCharge extends AbstractIceCharge {
    private static final int PLAYER_EFFECT_RADIUS = 3;
    private static final int DAMAGE = 3;

    // Factory constructor for registration
    public PlayerIceCharge(EntityType<? extends AbstractIceCharge> type, Level world) {
        super(type, world);
    }

    // Player-thrown constructor
    public PlayerIceCharge(LivingEntity shooter, Level level) {
        super(ModEntities.ICE_CHARGE.get(), shooter, level);
    }

    // Movement-specific constructor
    public PlayerIceCharge(Level level, double x, double y, double z, Vec3 movement) {
        super(ModEntities.ICE_CHARGE.get(), level);
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
