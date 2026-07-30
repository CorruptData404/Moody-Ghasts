package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


public abstract class AbstractIceChargeEntity extends AbstractHurtingProjectile implements ItemSupplier {

    protected abstract float getEffectRadius();
    protected abstract float getDamage();
    private final Set<BlockPos> recentlyConverted = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int CONVERSION_TIMEOUT = 2; // ticks
    private static final ResourceKey<DamageType> ICECHARGE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ice_charge"));

    // Constructor 1
    public AbstractIceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, Level world) {
        super(type, world);
    }

    // Movement vector constructor with owner
    protected AbstractIceChargeEntity(EntityType<? extends AbstractIceChargeEntity> type, LivingEntity owner, Vec3 movement, Level level) {
        super(type, owner, movement, level);
    }

    @Override
    public void tick() {
        // Store the current position before updating
        BlockPos prevBlockPos = this.blockPosition();
        super.tick();
        BlockPos newBlockPos = this.blockPosition();

        if (!level().isClientSide()) {

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

        if (!this.level().isClientSide()) {
            Vec3 location = result.getLocation();

            if (this.level() instanceof ServerLevel server) {
                // Get base radius and adjust for Nether
                float radius = getAdjustedRadius(server);

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
                            2,        // count
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
                applyIceEffects(centerPos, server, radius);
            }

            this.discard();
        }
    }


    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHit) {
        if (!this.level().isClientSide() && entityHit.getEntity() instanceof LivingEntity target) {

            if (target.isOnFire()) {
                target.extinguishFire();
                applyIceEffects(target.blockPosition(), (ServerLevel) this.level());
                return;
            }

            if (target.getType().builtInRegistryHolder().is(ModTags.Entities.FREEZE_IMMUNE)) return;

            DamageSource iceDamage = new DamageSource(
                    registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(ICECHARGE_DAMAGE));

            if (this.getOwner() instanceof LivingEntity livingOwner) {
                target.setLastHurtByMob(livingOwner);
            }

            if (this.level().dimension() == Level.NETHER) {
                if (target.isSensitiveToWater()) {
                    target.hurtServer((ServerLevel) this.level(), iceDamage, getDamage() * 2);
                }
            } else {
                target.hurtServer((ServerLevel) this.level(), iceDamage, getDamage());
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 2));
            }

            // Create ice effects around the hit entity
            applyIceEffects(target.blockPosition(), (ServerLevel) this.level());
        }
    }


    protected float getAdjustedRadius(ServerLevel server) {
        float baseRadius = getEffectRadius();
        return server.dimension() == Level.NETHER ? baseRadius / 2 : baseRadius;
    }

    protected void applyIceEffects(BlockPos center, ServerLevel server) {
        applyIceEffects(center, server, getAdjustedRadius(server));
    }

    protected void applyIceEffects(BlockPos center, ServerLevel server, float adjustedRadius) {
        int radius = (int) adjustedRadius;
        long radiusSq = (long) radius * radius;

        // Every position in the sphere is looked up up to 3 times: once as itself,
        // once as a neighbour's "above", and once as a neighbour's "below". Caching by
        // packed long position (BlockPos.asLong()) avoids re-querying the chunk/section
        // for a block already read, without the overhead or aliasing risk of
        // using mutable BlockPos objects as map keys.
        Map<Long, BlockState> stateCache = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        for (int x = -radius; x <= radius; x++) {
            long xSq = (long) x * x;
            for (int y = -radius; y <= radius; y++) {
                long xySq = xSq + (long) y * y;
                if (xySq > radiusSq) continue; // whole z-column is out of range, skip early
                for (int z = -radius; z <= radius; z++) {
                    if (xySq + (long) z * z > radiusSq) continue;

                    cursor.set(cx + x, cy + y, cz + z);
                    BlockPos pos = cursor.immutable();
                    BlockPos abovePos = pos.above();
                    BlockPos belowPos = pos.below();

                    BlockState state = cachedState(server, stateCache, pos);
                    BlockState aboveState = cachedState(server, stateCache, abovePos);
                    BlockState belowState = cachedState(server, stateCache, belowPos);

                    processFireExtinguishing(server, pos, state);
                    processSnowCreation(server, belowPos, belowState);
                    processBlockConversion(pos, server, state, aboveState, belowState);

                    stateCache.remove(pos.asLong());
                }
            }
        }
    }

    private BlockState cachedState(ServerLevel server, Map<Long, BlockState> cache, BlockPos pos) {
        return cache.computeIfAbsent(pos.asLong(), _ -> server.getBlockState(pos));
    }

    private boolean shouldTriggerInFluid(BlockPos pos) {
        Level level = this.level();

        // Check if actually in a fluid
        if (level.getFluidState(pos).isEmpty()) {
            return false;
        }

        // Check all adjacent blocks
        for (Direction dir : Direction.values()) {
            BlockPos adjacentPos = pos.relative(dir);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            // If a non-solid block that isn't a fluid source, return true
            if (adjacentState.getCollisionShape(level, adjacentPos).isEmpty() &&
                    !(adjacentState.is(Blocks.WATER) && adjacentState.getFluidState().isSource()) &&
                    !(adjacentState.is(Blocks.LAVA) && adjacentState.getFluidState().isSource())) {
                return true;
            }
        }
        return false;
    }

    private void processFireExtinguishing(ServerLevel server, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BaseFireBlock) {
            server.removeBlock(pos, false);
            server.playSound(
                    null,
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.7F,
                    1.6F + (server.getRandom().nextFloat() - server.getRandom().nextFloat()) * 0.4F
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
                    !belowState.is(BlockTags.CANNOT_SUPPORT_SNOW_LAYER)) {
                server.setBlockAndUpdate(snowPos, Blocks.SNOW.defaultBlockState());
            }
        }
    }

    private void processBlockConversion(BlockPos pos, ServerLevel server,
                                        BlockState state, BlockState aboveState, BlockState belowState) {
        if (state.is(Blocks.WATER) || state.is(Blocks.WATER_CAULDRON)) {
            handleWaterConversion(pos, server, state, aboveState);
        } else if (state.is(Blocks.LAVA)) {
            handleLavaConversion(pos, server, state, belowState);
        }

    }

    private void handleWaterConversion(BlockPos pos, ServerLevel server, BlockState state, BlockState aboveState) {
        // Handle regular water blocks
        if (state.getFluidState().isSource() &&
                !aboveState.is(Blocks.WATER) &&
                !aboveState.is(Blocks.FROSTED_ICE) &&
                !aboveState.is(Blocks.ICE)) {
            server.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
            server.scheduleTick(pos, Blocks.FROSTED_ICE, 200);
        } else if (state.is(Blocks.WATER_CAULDRON))  { // Handle Cauldrons with water
            int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            server.setBlockAndUpdate(pos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, waterLevel));
        }
    }


    private void handleLavaConversion(BlockPos pos, ServerLevel server, BlockState state, BlockState belowState) {
            // Convert lava
            if (state.getFluidState().isSource()) {
                server.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
            } else if (belowState.is(Blocks.SOUL_SOIL)) {
                server.setBlockAndUpdate(pos, Blocks.BASALT.defaultBlockState());
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
                    2.0F + (server.getRandom().nextFloat() - server.getRandom().nextFloat()) * 0.4F);
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