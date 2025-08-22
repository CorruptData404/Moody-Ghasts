package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MoodyIceChargeEntity extends AbstractIceChargeEntity {
    private float damage = 3;
    private int radius = 3;

    // Factory constructor for registration
    public MoodyIceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, Level world) {
        super(type, world);
    }

    // Ghast-thrown constructor with rage level
    public MoodyIceChargeEntity(Level level, LivingEntity shooter, Vec3 movement, int radius, float damage) {
        super(ModEntities.MOODY_ICE_CHARGE.get(), shooter, movement, level);
        this.damage = damage;
        this.radius = radius;
    }

    @Override
    protected int getEffectRadius() {
        return radius;
    }

    @Override
    protected float getDamage() {
        return damage;
    }
}