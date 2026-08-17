package ca.corruptdata.moodyghasts.entity.projectile.ice_charge;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MoodyIceChargeEntity extends AbstractIceChargeEntity {
    private final float damage;
    private final float radius;

    // Factory constructor for registration
    public MoodyIceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, Level world) {
        super(type, world);
        this.damage = 3;
        this.radius = 3;
    }

    // Ghast-thrown constructor with rage level
    public MoodyIceChargeEntity(Level level, LivingEntity shooter, float radius, float damage) {
        super(ModEntities.MOODY_ICE_CHARGE.get(), shooter, level);
        this.damage = damage;
        this.radius = radius;
    }

    @Override
    protected float getEffectRadius() {
        return radius;
    }

    @Override
    protected float getDamage() {
        return damage;
    }
}