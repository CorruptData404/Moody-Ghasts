package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.Vec3;

public class IceChargeEntity extends AbstractIceChargeEntity {
    private static final int EFFECT_RADIUS = 3;
    private static final float DAMAGE = 3;

    // Factory constructor for registration
    public IceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, Level world) {
        super(type, world);
    }


    public IceChargeEntity(LivingEntity shooter, Level level, double x, double y, double z) {
        super(ModEntities.ICE_CHARGE.get(), shooter, Vec3.ZERO, level);
        this.setPos(x, y, z);
    }

    // Movement-specific constructor
    public IceChargeEntity(Level level, double x, double y, double z, Vec3 movement) {
        super(ModEntities.ICE_CHARGE.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(movement);
    }
    @Override
    protected int getEffectRadius() {
        return EFFECT_RADIUS;
    }
    @Override
    protected float getDamage() {
        return DAMAGE;
    }

}