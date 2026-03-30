package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Optional;

public class GhastMoodHandler {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("moodyghasts", "speed_modifier");

    public static void adjustMood(HappyGhast ghast, float delta) {
        if (delta == 0.0) return;

        ParticleOptions particle = delta > 0F ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HAPPY_VILLAGER;
        float currentMood = ghast.getData(ModAttachments.MOOD);
        if (wouldCrossMoodThreshold(currentMood, delta)) spawnSurroundParticles(ghast, particle);

        float newMood = Mth.clamp(currentMood + delta, GhastMoodMap.MIN, GhastMoodMap.MAX);
        LOGGER.info("Adjusting mood by {} from {} to {}", delta, currentMood, newMood);
        ghast.setData(ModAttachments.MOOD, newMood);
    }

    public static boolean wouldCrossMoodThreshold(float currentMood, float delta) {
        float newMood = Mth.clamp(currentMood + delta, GhastMoodMap.MIN, GhastMoodMap.MAX);
        GhastMoodMap thresholds = GhastMoodMap.get();

        return !thresholds.getMoodOfValue(currentMood).equals(thresholds.getMoodOfValue(newMood));
    }
    @SubscribeEvent
    private void onEatingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;
        if (!ghast.getData(ModAttachments.IS_CONSUMING_FOOD)) return;

        int consumeTime = ghast.getData(ModAttachments.FOOD_CONSUME_TICKS);
        ghast.setData(ModAttachments.FOOD_CONSUME_TICKS, consumeTime + 1);

        // Every 4 ticks, play eating sound and particles
        if (consumeTime % 4 == 0) {
            ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);

            spawnMouthParticles(ghast, new ItemParticleOption(
                    ParticleTypes.ITEM,
                    ghast.getData(ModAttachments.CURRENT_FOOD).getDefaultInstance()
            ));
        }

        // Finish eating
        if (consumeTime >= 32) {
            float moodDelta = ghast.getData(ModAttachments.CURRENT_FOOD).builtInRegistryHolder()
                    .getData(ItemPropertyMap.MoodyConsumable.DATA_MAP).moodDelta();

            adjustMood(ghast, moodDelta);

            ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 1.0F, 1.0F);

            ghast.setData(ModAttachments.IS_CONSUMING_FOOD, false);
            ghast.setData(ModAttachments.CURRENT_FOOD, Items.AIR);
            ghast.setData(ModAttachments.FOOD_CONSUME_TICKS, 0);
        }
    }

    @SubscribeEvent
    private void onSpeedModifyTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
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

    @SubscribeEvent
    private void onMoodRegressionTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;

        float currentMood = ghast.getData(ModAttachments.MOOD);
        float baseMood = GhastMoodMap.getBaseMood();
        // Early return if already at base mood
        if (currentMood == baseMood) return;

        // Get the regression configuration for current mood state, if it exists
        Optional<GhastMoodMap.GhastMoodState.MoodRegression> regression = GhastMoodMap.get()
                .getMoodRegression(currentMood);

        if (regression.isEmpty()) return;

        // Check if regression should occur this tick based on chance_per_tick
        if (ghast.level().getRandom().nextFloat() > regression.get().chance_per_tick()) return;

        float delta = regression.get().delta();

        // If very close to base mood (within one delta), set it to base
        if (Math.abs(currentMood - baseMood) <= delta) {
            adjustMood(ghast, baseMood - currentMood);
        }
        else{
            adjustMood(ghast, currentMood > baseMood ? -delta : delta);
        }
    }

    @SubscribeEvent
    private void onTantrumTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;

        float mood = ghast.getData(ModAttachments.MOOD);
        int transformOnTick = GhastMoodMap.get().getTantrumTick(mood);

        if (transformOnTick > 0) {
            int tantrumTicks = ghast.getData(ModAttachments.TANTRUM_TICKS) + 1;
            ghast.setData(ModAttachments.TANTRUM_TICKS, tantrumTicks);

            if (tantrumTicks >= transformOnTick) {
                if (!net.neoforged.neoforge.event.EventHooks.canLivingConvert(ghast, EntityType.GHAST, t -> {}))
                    return;

                ServerLevel serverLevel = (ServerLevel) ghast.level();


                // Dismount all riders with short slow falling
                for (Entity passenger : new ArrayList<>(ghast.getPassengers())) {
                    if (passenger instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0)); // 10 seconds
                    }
                    passenger.stopRiding();
                }

                // Drop harness if equipped
                ItemStack harness = ghast.getItemBySlot(EquipmentSlot.BODY);
                if (!harness.isEmpty()) {
                    ghast.spawnAtLocation(serverLevel, harness);
                    ghast.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
                }

                // Drop 0–4 ghast tears
                int tearCount = serverLevel.random.nextInt(5);
                if (tearCount > 0) {
                    ItemStack tears = new ItemStack(Items.GHAST_TEAR, tearCount);
                    ghast.spawnAtLocation(serverLevel, tears);
                }

                // Convert to hostile ghast
                ghast.convertTo(EntityType.GHAST, ConversionParams.single(ghast, false, true), newGhast -> {
                    net.neoforged.neoforge.event.EventHooks.onLivingConvert(ghast, newGhast);

                    if (!ghast.isSilent()) {
                        serverLevel.playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                                SoundEvents.GHAST_HURT, SoundSource.HOSTILE, 1.0F, 1.0F);
                    }
                });
            }
        } else {
            ghast.setData(ModAttachments.TANTRUM_TICKS, 0);
        }
    }

    @SubscribeEvent
    private void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getAmount() * GhastMoodMap.get().settings().healMoodRate());
    }
    @SubscribeEvent
    private void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getNewDamage() * GhastMoodMap.get().settings().damageMoodRate());
    }

    public static void spawnSurroundParticles(HappyGhast ghast, ParticleOptions particleOption) {
        if (ghast.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 15; i++) {
                double d0 = ghast.getRandom().nextGaussian() * 0.02;
                double d1 = ghast.getRandom().nextGaussian() * 0.02;
                double d2 = ghast.getRandom().nextGaussian() * 0.02;
                serverLevel.sendParticles(
                        particleOption,
                        ghast.getRandomX(1.0),
                        ghast.getRandomY() + 1.0,
                        ghast.getRandomZ(1.0),
                        1, // particle count
                        d0, d1, d2, // velocity
                        0.0 // speed
                );
            }
        }
    }

    public static void spawnMouthParticles(HappyGhast ghast, ParticleOptions particleOption) {
        if (ghast.level() instanceof ServerLevel serverLevel) {
            Vec3 viewVec = ghast.getViewVector(1.0F);
            Vec3 mouthPos = new Vec3(
                    ghast.getX() + viewVec.x * 2.7,
                    ghast.getEyeY() - 1.5,
                    ghast.getZ() + viewVec.z * 2.7
            );
            for (int i = 0; i < 8; i++) {
                double d0 = ghast.getRandom().nextGaussian() * 0.02; // small horizontal spread
                double d1 = -0.1; // consistent downward velocity
                double d2 = ghast.getRandom().nextGaussian() * 0.02; // small horizontal spread
                serverLevel.sendParticles(
                        particleOption,
                        mouthPos.x,
                        mouthPos.y,
                        mouthPos.z,
                        1, // particle count
                        d0, d1, d2, // velocity
                        0.1 // slight speed variation
                );
            }
        }
    }
}
