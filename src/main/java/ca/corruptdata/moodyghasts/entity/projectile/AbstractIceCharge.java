package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.api.IceChargeConvertible;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ca.corruptdata.moodyghasts.item.ModItems;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;


public abstract class AbstractIceCharge extends AbstractHurtingProjectile implements ItemSupplier {

    protected abstract int getEffectRadius();
    protected abstract int getDamage();
    private final Set<BlockPos> recentlyConverted = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int CONVERSION_TIMEOUT = 2; // ticks




    // Constructor 1
    public AbstractIceCharge(EntityType<? extends AbstractIceCharge> type, Level world) {
        super(type, world);
    }

    // Constructor 2
    public AbstractIceCharge(EntityType<? extends AbstractIceCharge> type, LivingEntity shooter, Level world) {
        super(type,
                shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1,
                shooter.getZ(),
                world);
        this.setOwner(shooter);
    }

    @Override
    public void tick() {
        // Store the current position before updating
        BlockPos prevBlockPos = this.blockPosition();
        super.tick();
        BlockPos newBlockPos = this.blockPosition();
        
        // Server-side only operations
        if (!level().isClientSide) {

            // Clean up expired converted block positions
            if (level() instanceof ServerLevel) {
                recentlyConverted.clear();
            }
            
            // Check for fluid interactions when the entity moves to a new block
            if (!prevBlockPos.equals(newBlockPos) && shouldTriggerInFluid(newBlockPos)) {
                BlockHitResult hitResult = new BlockHitResult(
                    this.position(),
                    Direction.DOWN,
                    newBlockPos,
                    true
                );
                this.onHitBlock(hitResult);
                this.onHit(hitResult);
            }
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            Vec3 location = result.getLocation();

            if (this.level() instanceof ServerLevel server) {
                // Get base radius and adjust for Nether
                int radius = getAdjustedRadius(server);

                // Get the appropriate particle type based on dimension
                ParticleOptions particleType = server.dimension() == Level.NETHER ?
                        ParticleTypes.FALLING_WATER :
                        ParticleTypes.SNOWFLAKE;



                // Spawn particles in a sphere pattern
                RandomSource random = server.getRandom();
                for (int i = 0; i < 50; i++) {
                    double angle1 = random.nextDouble() * Math.PI * 2;
                    double angle2 = random.nextDouble() * Math.PI * 2;
                    double randRadius = random.nextDouble() * radius;

                    double x = location.x + randRadius * Math.sin(angle1) * Math.cos(angle2);
                    double y = location.y + randRadius * Math.sin(angle1) * Math.sin(angle2);
                    double z = location.z + randRadius * Math.cos(angle1);

                    // Calculate velocity away from center
                    double speedMultiplier = 0.2;
                    double vx = (x - location.x) * speedMultiplier;
                    double vy = (y - location.y) * speedMultiplier;
                    double vz = (z - location.z) * speedMultiplier;

                    server.sendParticles(
                            particleType,
                            x, y, z,  // position
                            1,        // count (1 particle per position)
                            vx, vy, vz,  // velocity
                            0.10      // speed
                    );
                }

                this.level().playSound(
                        null,
                        result.getLocation().x,
                        result.getLocation().y,
                        result.getLocation().z,
                        SoundEvents.PLAYER_HURT_FREEZE,
                        SoundSource.NEUTRAL,
                        2.0F,
                        0.4F / (this.level().getRandom().nextFloat() * 0.4F + 0.8F)
                );

                BlockPos centerPos = BlockPos.containing(location.x, location.y, location.z);
                applyIceEffects(centerPos, server);
            }

            this.discard();
        }
    }


    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHit) {
        super.onHitEntity(entityHit);
        if (!this.level().isClientSide && entityHit.getEntity() instanceof LivingEntity target) {

            if (target.isOnFire()) {
                target.clearFire();
                applyIceEffects(target.blockPosition(), (ServerLevel) this.level());
                return;
            }

            boolean isWaterVulnerable = target instanceof Blaze ||
                    target instanceof Strider ||
                    target instanceof EnderMan;

            //TODO: Make custom damageSource iceCharge
            if (this.level().dimension() == Level.NETHER) {
                if (isWaterVulnerable) {
                    target.hurtServer((ServerLevel) this.level(), this.damageSources().freeze(), getDamage() * 2);
                }
            }
            else {
                if (target instanceof IceChargeConvertible convertible) {
                    convertible.moodyghasts$startIceChargeConversion();
                }
                target.hurtServer((ServerLevel) this.level(), this.damageSources().freeze(), getDamage());
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 2));
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 2));
            }

            // Create ice effects around the hit entity
            applyIceEffects(target.blockPosition(), (ServerLevel) this.level());
            }
    }


    protected int getAdjustedRadius(ServerLevel server) {
        int baseRadius = getEffectRadius();
        return server.dimension() == Level.NETHER ? baseRadius / 2 : baseRadius;
    }

    protected void applyIceEffects(BlockPos center, ServerLevel server) {
        int radius = getAdjustedRadius(server);
        BlockPos.betweenClosedStream(
                        center.offset(-radius, -radius, -radius),
                        center.offset(radius, radius, radius)
                )
                .filter(pos -> pos.distSqr(center) <= (double) radius * radius)
                .forEach(pos -> {
                    BlockState state = server.getBlockState(pos);
                    BlockPos above = pos.above();
                    BlockState aboveState = server.getBlockState(above);
                    BlockPos below = pos.below();
                    BlockState belowState = server.getBlockState(below);

                    processFireExtinguishing(server, pos);
                    processSnowCreation(server, below, belowState);
                    processBlockConversion(pos, server, state, aboveState);
                });
    }

    private boolean shouldTriggerInFluid(BlockPos pos) {
        Level level = this.level();

        // Check if we're actually in a fluid
        if (level.getFluidState(pos).isEmpty()) {
            return false;
        }

        // Check all adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            // If we find a non-solid block that isn't a fluid source, return true
            if (adjacentState.getCollisionShape(level, adjacentPos).isEmpty() &&
                    !(adjacentState.is(Blocks.WATER) && adjacentState.getFluidState().isSource()) &&
                    !(adjacentState.is(Blocks.LAVA) && adjacentState.getFluidState().isSource())) {
                return true;
            }
        }
        return false;
    }

    private void processFireExtinguishing(ServerLevel server, BlockPos pos) {
        BlockState currentState = server.getBlockState(pos);
        if (currentState.getBlock() instanceof BaseFireBlock) {
            server.removeBlock(pos, false);
            server.playSound(
                    null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.7F,
                    1.6F + (server.random.nextFloat() - server.random.nextFloat()) * 0.4F
            );
        }
    }

    private void processSnowCreation(ServerLevel server, BlockPos below, BlockState belowState) {
        // Skip snow creation if the block below was recently converted
        if (server.dimension() == Level.NETHER || recentlyConverted.contains(below)) {
            return;
        }

        if (belowState.isFaceSturdy(server, below, Direction.UP)) {
            BlockPos snowPos = below.above();
            if (server.isEmptyBlock(snowPos) &&
                    !belowState.is(Blocks.FROSTED_ICE) &&
                    !belowState.is(net.minecraft.tags.BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
                server.setBlockAndUpdate(snowPos, Blocks.SNOW.defaultBlockState());
            }
        }
    }

    private void processBlockConversion(BlockPos pos, ServerLevel server,
                                        BlockState state, BlockState aboveState) {
        if (state.getBlock() instanceof LayeredCauldronBlock) {
            handleWaterConversion(pos, server, state, aboveState);
        }
        // Then check for regular water/lava blocks
        else if (state.is(Blocks.WATER)) {
            handleWaterConversion(pos, server, state, aboveState);
        } else if (state.is(Blocks.LAVA)) {
            handleLavaConversion(pos, server);
        }

    }

    private void handleWaterConversion(BlockPos pos, ServerLevel server, BlockState state, BlockState aboveState) {

        // Handle Cauldrons with water
        if (state.is(Blocks.WATER_CAULDRON)) {
            int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            server.setBlockAndUpdate(pos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, waterLevel));
        }

        // Handle regular water blocks
        if (state.getFluidState().isSource() &&
                !aboveState.is(Blocks.WATER) &&
                !aboveState.is(Blocks.FROSTED_ICE) &&
                !aboveState.is(Blocks.ICE)) {
            server.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
            server.scheduleTick(pos, Blocks.FROSTED_ICE, 200);
        }
    }


    private void handleLavaConversion(BlockPos pos, ServerLevel server) {
        BlockState state = server.getBlockState(pos);
        if (state.is(Blocks.LAVA)) {
            // Convert lava
            if (state.getFluidState().isSource()) {
                server.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            } else {
                server.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
            }

            // Mark this position as recently converted
            BlockPos immutablePos = pos.immutable();
            recentlyConverted.add(immutablePos);
            server.scheduleTick(pos, Blocks.AIR, CONVERSION_TIMEOUT);

            // Add steam particles
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    4, 0.3D, 0.3D, 0.3D, 0.0D);

            // Play the cooling sound
            server.playSound(null, pos,
                    SoundEvents.LAVA_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.2F,
                    2.0F + (server.random.nextFloat() - server.random.nextFloat()) * 0.4F);
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false; // Disable the fire effect
    }

    @Override
    protected void doWaterSplashEffect() {
        // Disable water splash
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return level().dimension() == Level.NETHER ?
                ParticleTypes.FALLING_WATER :
                ParticleTypes.SNOWFLAKE;
    }

    @Override
    public @NotNull ItemStack getItem() {
        return new ItemStack(ModItems.ICE_CHARGE.get());
    }
}