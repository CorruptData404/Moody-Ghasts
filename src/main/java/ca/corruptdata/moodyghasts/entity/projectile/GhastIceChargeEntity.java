package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class GhastIceChargeEntity extends AbstractIceChargeEntity {
    private static final int BASE_DAMAGE = 3;
    private static final int BASE_EFFECT_RADIUS = 3;
    //TODO: Define multipliers and rage level
    private static final float DAMAGE_MULTIPLIER = 1.5f;
    private static final float RADIUS_MULTIPLIER = 1.2f;
    private final int rageLevel;

    // Factory constructor for registration
    public GhastIceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, Level world) {
        super(type, world);
        this.rageLevel = 0;
    }

    // Ghast-thrown constructor with rage level
    public GhastIceChargeEntity(LivingEntity shooter, Level level, int rageLevel) {
        super(ModEntities.GHAST_ICE_CHARGE.get(), shooter, level);
        this.rageLevel = rageLevel;
    }

    @Override
    protected int getEffectRadius() {
        return BASE_EFFECT_RADIUS;
    }

    @Override
    protected int getDamage() {
        return BASE_DAMAGE;
    }
}