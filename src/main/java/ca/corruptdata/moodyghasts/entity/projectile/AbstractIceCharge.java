package ca.corruptdata.moodyghasts.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ca.corruptdata.moodyghasts.item.ModItems;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;



public abstract class AbstractIceCharge extends AbstractHurtingProjectile implements ItemSupplier {
    // 1) Factory constructor for registration
    public AbstractIceCharge(EntityType<? extends AbstractIceCharge> type, Level world) {
        super(type, world);
    }

    // 2) Thrown‑by‑player constructor: uses (type, x,y,z, world)
    public AbstractIceCharge(EntityType<? extends AbstractIceCharge> type, LivingEntity shooter, Level world) {
        super(type,
                shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.1,
                shooter.getZ(),
                world);
        this.setOwner(shooter);
    }

    @Override
    protected boolean shouldBurn() {
        return false; // Disable the fire effect
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel) {
            // Only check every few ticks to improve performance
            if (this.tickCount % 5 == 0) {
                BlockPos pos = this.blockPosition();
                BlockState state = this.level().getBlockState(pos);
                if ((state.is(Blocks.WATER) || state.is(Blocks.LAVA)) && !this.isRemoved()) {
                    // Create a BlockHitResult for the liquid block
                    BlockHitResult hitResult = new BlockHitResult(
                            this.position(),
                            Direction.DOWN, // Use a fixed direction since we're inside the block
                            pos,
                            true
                    );
                    this.onHitBlock(hitResult);
                    // Remove the entity after hitting liquid
                    this.discard();
                }
            }
        }
    }



    protected void onHit(HitResult result) {
        super.onHit(result);
        Level lvl = level();
        Vec3 pos = this.position();
        if (!lvl.isClientSide) {
            ServerLevel server = (ServerLevel) lvl;
            // Particle burst
            server.sendParticles(ParticleTypes.SNOWFLAKE,
                    pos.x, pos.y, pos.z,
                    30,    // count
                    0.5, 0.5, 0.5, // spread
                    0.02  // speed
            );
            server.sendParticles(ParticleTypes.CLOUD,
                    pos.x, pos.y, pos.z,
                    20, 0.4, 0.4, 0.4, 0.01);

            // Play sound
            server.playSound(null,
                    pos.x, pos.y, pos.z,
                    SoundEvents.GLASS_BREAK,
                    SoundSource.BLOCKS,
                    1.0F, 0.8F + lvl.random.nextFloat() * 0.2F
            );
        }
        discard();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.ICE_CHARGE.get());
    }
}
