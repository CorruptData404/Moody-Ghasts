package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GhastMoodHandler {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;
    private static final ResourceKey<LootTable> lootTableKey = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "events/happy_ghast_tantrum")
    );

    private static final long NOTICED_BY_BABY_GRACE_TICKS = 240L;

    public static void adjustMood(HappyGhast ghast, float delta) {
        if (delta == 0.0) return;

        float currentMood = ghast.getData(ModAttachments.MOOD);

        float newMood = Mth.clamp(currentMood + delta, GhastMoodMap.MIN, GhastMoodMap.MAX);

        if(Config.MOOD_LOGGING.get())
            LOGGER.info("Adjusting mood by {} from {} to {}", delta, currentMood, newMood);

        if (wouldCrossMoodThreshold(currentMood, delta)){
            ParticleOptions particle = delta > 0F ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HAPPY_VILLAGER;

            spawnSurroundParticles(ghast, particle,15);

            if(Config.MOOD_LOGGING.get())
                LOGGER.info("Ghast is now mood {}", GhastMoodMap.get().getMoodOfValue(newMood));
        }

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
        if (ghast.level().isClientSide()) return;
        if (ghast.isBaby()) return;
        if (!ghast.getData(ModAttachments.IS_CONSUMING_FOOD)) return;

        int consumeTime = ghast.getData(ModAttachments.FOOD_CONSUME_TICKS);
        ghast.setData(ModAttachments.FOOD_CONSUME_TICKS, consumeTime + 1);

        Item foodItem = ghast.getData(ModAttachments.CURRENT_FOOD);
        ItemPropertyMap.MoodyConsumable foodData = foodItem.builtInRegistryHolder().getData(ItemPropertyMap.MoodyConsumable.DATA_MAP);
        // Every 4 ticks, play eating sound and particles
        if (consumeTime % 4 == 0) {
            ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);

            spawnMouthParticles(ghast, new ItemParticleOption(ParticleTypes.ITEM, foodItem),8);
        }

        // Finish eating
        if (consumeTime >= foodData.consumeTick()) {

            if(GhastMovementHandler.tryTeleportGhastSafely(ghast, foodItem.getDefaultInstance(), foodData.rtpDiameter()))
                spawnSurroundParticles(ghast, ParticleTypes.PORTAL,600);

            adjustMood(ghast, foodData.moodDelta());

            ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 1.0F, 1.0F);

            ghast.setData(ModAttachments.IS_CONSUMING_FOOD, false);
            ghast.setData(ModAttachments.CURRENT_FOOD, Items.AIR);
            ghast.setData(ModAttachments.FOOD_CONSUME_TICKS, 0);
        }
    }

    @SubscribeEvent
    private void onBabyFollowAdultTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast babyGhast)) return;
        if (babyGhast.level().isClientSide()) return;
        if (!babyGhast.isBaby()) return;

        babyGhast.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT).ifPresent(adultEntity -> {
            if (adultEntity instanceof HappyGhast adultGhast) {
                adultGhast.setData(ModAttachments.LAST_NOTICED_BY_BABY_TICK, adultGhast.level().getGameTime());
            }
        });
    }

    @SubscribeEvent
    private void onMoodRegressionTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide()) return;
        if (ghast.isBaby()) return;

        float currentMood = ghast.getData(ModAttachments.MOOD);
        GhastMoodMap moodMap = GhastMoodMap.get();

        // Get the regression configuration for current mood state, if it exists
        Optional<GhastMoodMap.GhastMoodState.MoodRegression> regression = moodMap.getMoodRegression(currentMood);

        if (regression.isEmpty()) return;

        // Check if regression should occur this tick based on chance_per_tick
        if (ghast.level().getRandom().nextFloat() > regression.get().chance_per_tick()) return;

        long lastNoticedTick = ghast.getData(ModAttachments.LAST_NOTICED_BY_BABY_TICK);
        boolean recentlyNoticedByBaby = lastNoticedTick >= 0
                && (ghast.level().getGameTime() - lastNoticedTick) <= NOTICED_BY_BABY_GRACE_TICKS;

        float baseMood = recentlyNoticedByBaby ? moodMap.settings().noticedByBabyBaseMood() : moodMap.settings().baseMood();

        if(recentlyNoticedByBaby && Config.MOOD_LOGGING.get())
            LOGGER.info("Ghast was noticed by a baby ghast {} ticks ago (grace window: {} ticks) - using modified base mood {}",
                    ghast.level().getGameTime() - lastNoticedTick, NOTICED_BY_BABY_GRACE_TICKS, baseMood);

        if (currentMood == baseMood) return;

        float delta = regression.get().delta();
        if (recentlyNoticedByBaby) {
            delta *= currentMood > baseMood
                    ? moodMap.settings().noticedByBabyHappierMult()
                    : moodMap.settings().noticedByBabyAngrierMult();
        }


        // If very close to base mood (within one delta), set it to base
        if (Math.abs(currentMood - baseMood) <= delta)
            adjustMood(ghast, baseMood - currentMood);
        else
            adjustMood(ghast, currentMood > baseMood ? -delta : delta);
    }

    @SubscribeEvent
    private void onTantrumTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide()) return;
        if (ghast.isBaby()) return;

        float mood = ghast.getData(ModAttachments.MOOD);
        GhastMoodMap moodMap = GhastMoodMap.get();
        int transformOnTick = moodMap.getTantrumTick(mood);

        if (transformOnTick > 0) {
            int tantrumTicks = ghast.getData(ModAttachments.TANTRUM_TICKS) + 1;
            ghast.setData(ModAttachments.TANTRUM_TICKS, tantrumTicks);
            ServerLevel serverLevel = (ServerLevel) ghast.level();

            if (tantrumTicks >= transformOnTick) {
                if (!net.neoforged.neoforge.event.EventHooks.canLivingConvert(ghast, EntityTypes.GHAST, _ -> {}))
                    return;

                // Dismount all riders with short slow falling
                for (Entity passenger : new ArrayList<>(ghast.getPassengers())) {
                    if (passenger instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0)); // 5 seconds
                    }
                    passenger.stopRiding();
                }

                // Drop harness if equipped
                ItemStack harness = ghast.getItemBySlot(EquipmentSlot.BODY);
                if (!harness.isEmpty()) {
                    ghast.spawnAtLocation(serverLevel, harness);
                    ghast.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
                }

                // Drop tantrum loot
                LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey);

                LootParams lootParams = new LootParams.Builder(serverLevel)
                        .create(LootContextParamSets.EMPTY);

                for (ItemStack drop : lootTable.getRandomItems(lootParams)) {
                    if (!drop.isEmpty()) {
                        ghast.spawnAtLocation(serverLevel, drop);
                    }
                }

                // Convert to hostile ghast
                if(serverLevel.getDifficulty() != Difficulty.PEACEFUL)
                {
                    ghast.convertTo(EntityTypes.GHAST, ConversionParams.single(ghast, false, true),
                            newGhast -> {net.neoforged.neoforge.event.EventHooks.onLivingConvert(ghast, newGhast);});
                }
                else{
                    //in peaceful difficulty
                    ghast.setData(ModAttachments.TANTRUM_TICKS, 0);
                    adjustMood(ghast, moodMap.settings().peacefulTantrumCatharsisDelta());
                }

                spawnSurroundParticles(ghast, ParticleTypes.ANGRY_VILLAGER,30);
                if (!ghast.isSilent()) {
                    serverLevel.playSound(ghast, ghast.getX(), ghast.getY(), ghast.getZ(),
                            SoundEvents.GHAST_HURT, SoundSource.HOSTILE, 1.0F, 1.0F);
                }

                notifyNearbyGhastsOfTantrum(ghast, moodMap);
            }
            else if (tantrumTicks % 50 == 0 && !(ghast.isSilent()
                    || ghast.getData(ModAttachments.IS_CHARGING)
                    || ghast.getData(ModAttachments.IS_FIRING)
                    || ghast.getData(ModAttachments.IS_CONSUMING_FOOD))) {
                serverLevel.playSound(ghast, ghast.getX(), ghast.getY(), ghast.getZ(),
                        SoundEvents.GHAST_AMBIENT, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        } else {
            ghast.setData(ModAttachments.TANTRUM_TICKS, 0);
        }
    }

    private void notifyNearbyGhastsOfTantrum(HappyGhast ghast, GhastMoodMap moodMap) {
        var settings = moodMap.settings();
        if (settings.seeTantrumDelta() == 0F) return;

        AABB searchBox = ghast.getBoundingBox().inflate(settings.moodEventRadius());
        List<HappyGhast> nearbyAdults = ghast.level().getEntitiesOfClass(
                HappyGhast.class, searchBox, adult -> !adult.isBaby() && adult != ghast
        );

        for (HappyGhast adult : nearbyAdults) {
            adjustMood(adult, settings.seeTantrumDelta());
        }
    }

    @SubscribeEvent
    private void onGhastDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof HappyGhast deceasedGhast)) return;
        if (deceasedGhast.level().isClientSide()) return;

        var settings = GhastMoodMap.get().settings();
        boolean babyDied = deceasedGhast.isBaby();

        float killDelta = babyDied ? settings.killBabyDelta() : settings.killAdultDelta();
        float witnessDelta = babyDied ? settings.seeBabyDeathDelta() : settings.seeAdultDeathDelta();

        // If a happy ghast or player riding a happy ghast is responsible for the kill apply killDelta instead of witnessDelta
        DamageSource damageSource = event.getSource();
        Entity killer = damageSource.getEntity();
        Entity directEntity = damageSource.getDirectEntity();

        HappyGhast killerGhast = null;
        if (directEntity instanceof Projectile projectile) {
            Optional<UUID> owningGhastId = projectile.getData(ModAttachments.OWNING_GHAST);
            if (owningGhastId.isPresent() && deceasedGhast.level() instanceof ServerLevel serverLevel) {
                Entity owningGhast = serverLevel.getEntity(owningGhastId.get());
                if (owningGhast instanceof HappyGhast ghast && !ghast.isBaby() && ghast != deceasedGhast) {
                    killerGhast = ghast;
                }
            }
        } else if (killer instanceof HappyGhast adultKiller && !adultKiller.isBaby() && adultKiller != deceasedGhast) {
            // Adult HappyGhasts have no direct attack in vanilla, so this branch is usually unreachable
            killerGhast = adultKiller;
        } else if (killer != null && killer.getVehicle() instanceof HappyGhast mount
                && !mount.isBaby() && mount != deceasedGhast) {
            killerGhast = mount;
        }

        if (killerGhast != null && killDelta != 0F) {
            if (Config.MOOD_LOGGING.get())
                LOGGER.info("Ghast {} was killed by adult ghast {}, applying mood delta {} regardless of range",
                        deceasedGhast.getUUID(), killerGhast.getUUID(), killDelta);

            adjustMood(killerGhast, killDelta);
        }

        // Everyone else nearby gets the smaller "witnessed a death" adjustment.
        if (witnessDelta == 0F) return;

        HappyGhast finalKillerGhast = killerGhast;
        AABB searchBox = deceasedGhast.getBoundingBox().inflate(settings.moodEventRadius());
        List<HappyGhast> nearbyAdults = deceasedGhast.level().getEntitiesOfClass(
                HappyGhast.class,
                searchBox,
                adult -> !adult.isBaby() && adult != deceasedGhast && !adult.equals(finalKillerGhast)
        );

        for (HappyGhast adult : nearbyAdults) {
            if (Config.MOOD_LOGGING.get())
                LOGGER.info("Ghast {} died near adult ghast {}, applying mood delta {}",
                        deceasedGhast.getUUID(), adult.getUUID(), witnessDelta);

            adjustMood(adult, witnessDelta);
        }
    }

    @SubscribeEvent
    private void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getAmount() * GhastMoodMap.get().settings().healMoodMult());
    }
    @SubscribeEvent
    private void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getInflictedDamage() * GhastMoodMap.get().settings().damageMoodMult());
    }

    public static void spawnSurroundParticles(HappyGhast ghast, ParticleOptions particleOption, int number) {
        if (ghast.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < number; i++) {
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

    public static void spawnMouthParticles(HappyGhast ghast, ParticleOptions particleOption, int number) {
        if (ghast.level() instanceof ServerLevel serverLevel) {
            Vec3 viewVec = ghast.getViewVector(1.0F);
            Vec3 mouthPos = new Vec3(
                    ghast.getX() + viewVec.x * 2.7,
                    ghast.getEyeY() - 1.5,
                    ghast.getZ() + viewVec.z * 2.7
            );
            for (int i = 0; i < number; i++) {
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
