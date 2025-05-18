package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerIceCharge extends AbstractIceCharge {
    protected double accelerationPower = 0.1; // Default acceleration power
    
    // Factory constructor for registration
    public PlayerIceCharge(EntityType<? extends AbstractIceCharge> type, Level world) {
        super(type, world);
        initializeDefaults();
    }

    // Player-thrown constructor
    public PlayerIceCharge(LivingEntity shooter, Level level, double x, double y, double z) {
        super(ModEntities.ICE_CHARGE.get(), shooter, level);
        initializeDefaults();
    }

    // Movement-specific constructor
    public PlayerIceCharge(Level level, double x, double y, double z, Vec3 movement) {
        super(ModEntities.ICE_CHARGE.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(movement);
        initializeDefaults();
    }

    private void initializeDefaults() {
        this.accelerationPower = 0.1;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHit) {
        super.onHitEntity(entityHit);
        if (!this.level().isClientSide && entityHit.getEntity() instanceof LivingEntity target) {
            // Apply freezing effects to the hit entity
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 2));
            
            // Create ice effects around the hit entity
            applyIceEffects(target.blockPosition(), (ServerLevel) this.level(), 2);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHit) {
        super.onHitBlock(blockHit);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel server) {
            applyIceEffects(blockHit.getBlockPos(), server, 2);
        }
    }

    private void applyIceEffects(BlockPos center, ServerLevel server, int radius) {
        BlockPos.betweenClosedStream(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        )
        .filter(pos -> pos.distSqr(center) <= (double) radius * radius)
        .forEach(pos -> {
            // Snow Layer Creation
            BlockPos below = pos.below();
            BlockState belowState = server.getBlockState(below);
            if (belowState.isFaceSturdy(server, below, Direction.UP)) {
                BlockPos snowPos = below.above();
                // Check if snow can be placed here
                if (server.isEmptyBlock(snowPos) && 
                    !belowState.is(Blocks.FROSTED_ICE) && 
                    !belowState.is(net.minecraft.tags.BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
                    server.setBlockAndUpdate(snowPos, Blocks.SNOW.defaultBlockState());
                }
            }

            // Liquid Conversion - Only source blocks
            BlockState state = server.getBlockState(pos);
            if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
                server.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                server.scheduleTick(pos, Blocks.FROSTED_ICE, 200);
            } else if (state.is(Blocks.LAVA)) {
                if(state.getFluidState().isSource())
                    server.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                else
                    server.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
            }
        });
    }
}