package ca.corruptdata.moodyghasts.item.custom;

import ca.corruptdata.moodyghasts.entity.projectile.PlayerIceCharge;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.DispenserBlock;

public class IceChargeItem extends Item implements ProjectileItem {
    public static final float PROJECTILE_SHOOT_POWER = 1.5F;

    public IceChargeItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverlevel) {
            Projectile.spawnProjectileFromRotation(
                (p_level, p_shooter, p_projectile) -> new PlayerIceCharge(
                    player, level, player.position().x(), player.getEyePosition().y(), player.position().z()
                ),
                serverlevel,
                itemstack,
                player,
                0.0F,
                PROJECTILE_SHOOT_POWER,
                1.0F
            );
        }

        level.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.WIND_CHARGE_THROW,
            SoundSource.NEUTRAL,
            0.5F,
            0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        player.awardStat(Stats.ITEM_USED.get(this));
        itemstack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public PlayerIceCharge asProjectile(Level level, Position pos, ItemStack stack, Direction dir) {
        RandomSource randomsource = level.getRandom();
        double d0 = randomsource.triangle(dir.getStepX(), 0.11485000000000001);
        double d1 = randomsource.triangle(dir.getStepY(), 0.11485000000000001);
        double d2 = randomsource.triangle(dir.getStepZ(), 0.11485000000000001);
        Vec3 vec3 = new Vec3(d0, d1, d2);
        return new PlayerIceCharge(level, pos.x(), pos.y(), pos.z(), vec3);
    }

    @Override
    public void shoot(Projectile projectile, double x, double y, double z, float velocity, float inaccuracy) {
        projectile.shoot(x, y, z, velocity, inaccuracy);
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return DispenseConfig.builder()
                .positionFunction((src, vel) -> DispenserBlock.getDispensePosition(src, 1.0, Vec3.ZERO))
                .uncertainty(6.6666665F)
                .power(1.0F)
                .overrideDispenseEvent(1051)
                .build();
    }
}