package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slf4j.Logger;

import java.util.Set;

public class GhastMovementHandler {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;
    private static final Identifier SPEED_MODIFIER_ID =
            Identifier.fromNamespaceAndPath("moodyghasts", "speed_modifier");

    @SubscribeEvent
    private void onSpeedModifyTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide()) return;
        if (ghast.isBaby()) return;

        AttributeInstance speedAttribute = ghast.getAttribute(Attributes.FLYING_SPEED);
        if (speedAttribute == null) return;

        float targetSpeed = GhastMoodMap.get().getSpeedModifier(ghast.getData(ModAttachments.MOOD));
        boolean hasModifier = speedAttribute.hasModifier(SPEED_MODIFIER_ID);

        if (targetSpeed != 0.0f) {
            if (!hasModifier || speedAttribute.getModifier(SPEED_MODIFIER_ID).amount() != targetSpeed) {
                speedAttribute.removeModifier(SPEED_MODIFIER_ID);
                speedAttribute.addTransientModifier(new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        targetSpeed,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        } else if (hasModifier) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    private static final int GHAST_SIZE = 3;      // 3x3x3 ghast
    private static final int RIDER_CLEARANCE = 2; // extra height for riders
    private static final int MAX_ATTEMPTS = 10;

    public static boolean tryTeleportGhastSafely(HappyGhast ghast, ItemStack stack, float diameter) {
        Level level = ghast.level();
        if (level.isClientSide()) return false;
        if (!(level instanceof ServerLevel serverLevel)) return false;
        if (diameter <= 0) return false;

        RandomSource random = ghast.getRandom();

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            double xx = ghast.getX() + (random.nextDouble() - 0.5) * diameter;
            double yy = Mth.clamp(
                    ghast.getY() + (random.nextDouble() - 0.5) * diameter,
                    level.getMinY(),
                    level.getMinY() + serverLevel.getLogicalHeight() - 1
            );
            double zz = ghast.getZ() + (random.nextDouble() - 0.5) * diameter;

            BlockPos candidate = BlockPos.containing(xx, yy, zz);

            if (!isSafeGhastLocation(level, candidate)) continue;

            // Fire the event since bypassing randomTeleport's own event firing
            EntityTeleportEvent.ItemConsumption event =
                    new EntityTeleportEvent.ItemConsumption(ghast, stack, xx, yy, zz);

            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                return false; // a listener blocked the teleport, stop trying
            }

            Vec3 oldPos = ghast.position();

            ghast.teleportTo(
                    serverLevel,
                    event.getTargetX(), event.getTargetY(), event.getTargetZ(),
                    Set.of(),
                    ghast.getYRot(),
                    ghast.getXRot(),
                    false
            );

            level.gameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Context.of(ghast));
            level.playSound(
                    null,
                    ghast.getX(), ghast.getY(), ghast.getZ(),
                    SoundEvents.CHORUS_FRUIT_TELEPORT,
                    SoundSource.NEUTRAL
            );
            ghast.resetFallDistance();
            ghast.resetCurrentImpulseContext();
            return true;
        }
        return false;
    }

    private static boolean isSafeGhastLocation(Level level, BlockPos center) {
        int radius = GHAST_SIZE / 2;
        int height = GHAST_SIZE + RIDER_CLEARANCE;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y < height; y++) {
                    BlockPos pos = center.offset(x, y, z);

                    if (!level.isLoaded(pos)) return false;

                    BlockState state = level.getBlockState(pos);
                    if (!state.getCollisionShape(level, pos).isEmpty()) {
                        return false; // solid block in the clearance zone
                    }
                }
            }
        }
        return true;
    }
}
