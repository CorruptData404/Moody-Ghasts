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
    private final float mood;

    // Factory constructor for registration
    public GhastIceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, Level world) {
        super(type, world);
        this.mood = 0;
    }

    // Ghast-thrown constructor with rage level
    public GhastIceChargeEntity(LivingEntity shooter, Level level, float mood) {
        super(ModEntities.GHAST_ICE_CHARGE.get(), shooter, level);
        this.mood = mood;
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