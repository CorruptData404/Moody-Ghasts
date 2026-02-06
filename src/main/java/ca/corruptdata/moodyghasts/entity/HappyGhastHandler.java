package ca.corruptdata.moodyghasts.entity;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.datamap.GhastMoodMap;
import ca.corruptdata.moodyghasts.datamap.ItemPropertyMap;
import ca.corruptdata.moodyghasts.entity.projectile.MoodyIceChargeEntity;
import ca.corruptdata.moodyghasts.entity.projectile.MoodyWindChargeEntity;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import ca.corruptdata.moodyghasts.ModTags;
import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.Snowball;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Optional;

public class HappyGhastHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final float BASE_MOOD = 0.25f;
    //Should always be Negative
    private static final float HEALED_MOOD_MULTIPLIER = -0.02f;
    //Should always be Positive
    private static final float DAMAGED_MOOD_MULTIPLIER = 0.02f;

    private static final AttributeModifier excitedSpeedModifier = new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath("moodyghasts", "excited_speed_modifier"),
            0.05,
            AttributeModifier.Operation.ADD_VALUE
    );


    // ============================================================
    // Event Handlers - Player Interactions
    // ============================================================

    @SubscribeEvent
    private void onRiderUseProjectile(PlayerInteractEvent.RightClickItem event) {
        ItemStack projectileItem = event.getItemStack();
        if (!projectileItem.is(ModTags.Items.HAPPY_GHAST_PROJECTILES)) return;
        Player player = event.getEntity();
        if (!(player.getVehicle() instanceof HappyGhast ghast)) return;
        if (player != ghast.getControllingPassenger()) return;
        event.setCanceled(true);
        if (isBusy(ghast)) return;

        ghast.setData(ModAttachments.IS_CHARGING, true);
        ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, 1); // Start charging
        ghast.setData(ModAttachments.CURRENT_PROJECTILE, projectileItem.getItem());
        ghast.setData(ModAttachments.PROJECTILE_OWNER, player);

        consumePlayerItem(event.getEntity(), projectileItem);
    }

    @SubscribeEvent
    private void onRiderFeed(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemTags.HAPPY_GHAST_FOOD)) return;
        if (!(event.getEntity().getVehicle() instanceof HappyGhast ghast)) return;
        if (event.getEntity() != ghast.getControllingPassenger()) return;
        if(stack.getItem().builtInRegistryHolder().getData(ItemPropertyMap.Consumable.DATA_MAP) == null) return;
        event.setCanceled(true);
        if (isBusy(ghast)) return;

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        consumePlayerItem(event.getEntity(), stack);

    }

    @SubscribeEvent
    private void onInteractFeed(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemTags.HAPPY_GHAST_FOOD)) return;
        if (!(event.getTarget() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        if (isBusy(ghast)) return;
        if(stack.getItem().builtInRegistryHolder().getData(ItemPropertyMap.Consumable.DATA_MAP) == null) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        consumePlayerItem(event.getEntity(), stack);
    }

    // ============================================================
    // Event Handlers - Tick Updates
    // ============================================================


    @SubscribeEvent
    private void onChargeTick(EntityTickEvent.Post event) {

        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;
        if (!ghast.getData(ModAttachments.IS_CHARGING)) return;

        int chargeTime = ghast.getData(ModAttachments.PROJECTILE_CHARGE_TICK);
        Player player = (Player) ghast.getControllingPassenger();

        if(player == ghast.getData(ModAttachments.PROJECTILE_OWNER) && !(chargeTime > 20)){
            ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, chargeTime + 1);
        }
        else
        {
            ghast.setData(ModAttachments.IS_CHARGING,false);
            ghast.setData(ModAttachments.CURRENT_PROJECTILE, Items.AIR);
            ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, 0);
        }

        if(chargeTime == 10){
            ghast.level().levelEvent(ghast, 1015, ghast.blockPosition(), 0);
        }
        else if(chargeTime == 20){
            shoot(player, ghast, ghast.getData(ModAttachments.CURRENT_PROJECTILE));
            ghast.setData(ModAttachments.IS_CHARGING,false);
            ghast.setData(ModAttachments.CURRENT_PROJECTILE, Items.AIR);
            ghast.setData(ModAttachments.PROJECTILE_CHARGE_TICK, 0);
        }
    }

    @SubscribeEvent
    private void onSnowBarrageTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (!ghast.getData(ModAttachments.IS_SNOWBALL_BARRAGE)) return;
        if (!(ghast.getControllingPassenger() instanceof Player player)) {
            ghast.setData(ModAttachments.IS_SNOWBALL_BARRAGE, false);
            ghast.setData(ModAttachments.SNOWBALLS_LEFT, 0);
            return;
        }

        int snowballsLeft = ghast.getData(ModAttachments.SNOWBALLS_LEFT);
        int nextDelay = ghast.getData(ModAttachments.SNOWBALL_COOLDOWN);

        if (snowballsLeft <= 0) {
            ghast.setData(ModAttachments.IS_SNOWBALL_BARRAGE, false);
            return;
        }

        // Calculate progress (1.0 -> 0.0)
        float progress = (float) snowballsLeft / BASE_SNOWBALL_COUNT;

        // Calculate logarithmic delay (increases as progress decreases)
        // Maps progress from 1.0->0.0 to 0->5 logarithmically
        float delayFactor = -2.0f * (float)Math.log(progress + 0.1f);
        int delay = Math.max(0, Math.min(5, (int)delayFactor));

        // Check if should shoot this tick
        if (nextDelay > 0) {
            ghast.setData(ModAttachments.SNOWBALL_COOLDOWN, nextDelay - 1);
            return;
        }

        // Calculate spawn position
        Vec3 spawnPos = getProjectileSpawnPos(ghast);

        // Create snowball
        Snowball snowball = new Snowball(ghast.level(), spawnPos.x(), spawnPos.y(), spawnPos.z(),
                new ItemStack(Items.SNOWBALL));
        snowball.setOwner(player);

        // Calculate base direction vector
        Vec3 direction = getPlayerAimVector(player);

        float spread = 0.11485f;
        RandomSource random = ghast.getRandom();
        Vec3 spreadVector = new Vec3(
                random.triangle(direction.x, spread),
                random.triangle(direction.y, spread),
                random.triangle(direction.z, spread)
        );

        // Calculate speed based on progress (faster at start, slower at the end)
        // Maps progress from 1.0->0.0 to 1.5->0.8 logarithmically
        float speedFactor = 0.8f + (0.7f * (float)Math.log10(progress + 0.1f) + 0.7f);

        // Set final velocity
        snowball.setDeltaMovement(spreadVector.multiply(speedFactor, speedFactor, speedFactor));

        // Spawn the snowball
        ghast.level().addFreshEntity(snowball);

        ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F,
                0.4F / (ghast.level().getRandom().nextFloat() * 0.4F + 0.8F));

        // Adjust mood
        adjustMood(ghast, -0.25F);

        // Update counters
        ghast.setData(ModAttachments.SNOWBALLS_LEFT, snowballsLeft - 1);
        ghast.setData(ModAttachments.SNOWBALL_COOLDOWN, delay);

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

            // TODO: make CURRENT_FOOD serialized so this isn't needed
            // Prevents crash upon server restart while eating
            if(ghast.getData(ModAttachments.CURRENT_FOOD) != Items.AIR) {
                spawnMouthParticles(ghast, new ItemParticleOption(
                        ParticleTypes.ITEM,
                        ghast.getData(ModAttachments.CURRENT_FOOD).getDefaultInstance()
                ));
            }
        }

        // Finish eating
        if (consumeTime >= 32) {
            float moodDelta = ghast.getData(ModAttachments.CURRENT_FOOD).builtInRegistryHolder()
                    .getData(ItemPropertyMap.Consumable.DATA_MAP).moodDelta();

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

        float speedModifier = GhastMoodMap.get().getSpeedModifier(ghast.getData(ModAttachments.MOOD));

        boolean hasModifier = speedAttribute.hasModifier(excitedSpeedModifier.id());

        if (speedModifier != 0.0f) {
            if (!hasModifier || speedAttribute.getModifier(excitedSpeedModifier.id()).amount() != speedModifier) {
                if (hasModifier) {
                    speedAttribute.removeModifier(excitedSpeedModifier.id());
                }
                AttributeModifier newModifier = new AttributeModifier(
                        excitedSpeedModifier.id(),
                        speedModifier,
                        AttributeModifier.Operation.ADD_VALUE
                );
                speedAttribute.addTransientModifier(newModifier);
            }
        } else if (hasModifier) {
            speedAttribute.removeModifier(excitedSpeedModifier.id());
        }
    }

    @SubscribeEvent
    private void onMoodRegressionTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;

        float currentMood = ghast.getData(ModAttachments.MOOD);
        // Early return if already at base mood
        if (currentMood == BASE_MOOD) return;

        // Get the regression configuration for current mood state, if it exists
        Optional<GhastMoodMap.GhastMoodState.MoodRegression> regression = GhastMoodMap.get()
                .getMoodRegression(currentMood);

        if (regression.isEmpty()) return;

        // Check if regression should occur this tick based on chance_per_tick
        if (ghast.level().getRandom().nextFloat() > regression.get().chance_per_tick()) return;

        float delta = regression.get().delta();

        // If very close to base mood (within one delta), set it to base
        if (Math.abs(currentMood - BASE_MOOD) <= delta) {
            adjustMood(ghast, BASE_MOOD - currentMood);
            return;
        }

        // Otherwise, apply normal regression
        float adjustment = currentMood > BASE_MOOD ? -delta : delta;
        adjustMood(ghast, adjustment);
    }

    @SubscribeEvent
    private void onTantrumTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;

        float mood = ghast.getData(ModAttachments.MOOD);
        int transformOnTick = GhastMoodMap.get().getMoodsTantrumTick(mood);

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

    // ============================================================
    // Event Handlers - Health based Mood Adjustments
    // ============================================================

    @SubscribeEvent
    private void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getAmount() * HEALED_MOOD_MULTIPLIER);
    }
    @SubscribeEvent
    private void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getNewDamage() * DAMAGED_MOOD_MULTIPLIER);
    }

    // ============================================================
    // Shooting Logic
    // ============================================================

    private void shoot(Player player, HappyGhast ghast, Item projectileItem) {
        applySharedCooldown(player, projectileItem);
        float mood = ghast.getData(ModAttachments.MOOD);
        float projMultiplier = GhastMoodMap.get().getMoodsProjMultiplier(mood);

        // Get projectile data from datamap
        var holder = projectileItem.builtInRegistryHolder();
        var projectileData = holder.getData(ItemPropertyMap.Projectile.DATA_MAP);
        assert projectileData != null;

        if (projectileItem instanceof IceChargeItem
                || projectileItem instanceof WindChargeItem
                || projectileItem instanceof FireChargeItem) {
            shootChargeProjectile(player, ghast, projectileItem, projMultiplier, projectileData);
        } else if (projectileItem == Items.POWDER_SNOW_BUCKET) {
            int baseCount = projectileData.baseSnowballCount();
            int totalSnowballs = (int) (baseCount * projMultiplier);

            LOGGER.info("Base snowball count: {}", baseCount);
            LOGGER.info("Total snowball count: {}", totalSnowballs);
            ghast.setData(ModAttachments.SNOWBALLS_LEFT, totalSnowballs);
            ghast.setData(ModAttachments.IS_SNOWBALL_BARRAGE, true);
            ghast.setData(ModAttachments.SNOWBALL_COOLDOWN, 0);
        }

        // Apply mood delta from projectile
        adjustMood(ghast, projectileData.moodDelta());
    }

    private void shootChargeProjectile(Player player, HappyGhast ghast, Item projectileItem,
                                 float projMultiplier, ItemPropertyMap.Projectile projectileData) {
        Level level = ghast.level();
        Vec3 spawnPos = getProjectileSpawnPos(ghast);
        Vec3 movement = getPlayerAimVector(player);
        AbstractHurtingProjectile projectile;

        switch (projectileItem) {
            case IceChargeItem iceChargeItem -> {
                ProjectileScaling scaling = ProjectileScaling.ICE;
                int radius = (int) (scaling.baseRadius() * moodMultiplier * scaling.moodMultiplier());
                float strength = scaling.baseStrength() * moodMultiplier * scaling.moodMultiplier();
                LOGGER.info("Ice Charge Radius: {}, Strength: {}", radius, strength);
                projectile = new MoodyIceChargeEntity(level, player, movement, radius, strength);
            }
            case WindChargeItem windChargeItem -> {
                ProjectileScaling scaling = ProjectileScaling.WIND;
                float radius = scaling.baseRadius() * moodMultiplier * scaling.moodMultiplier();
                float strength = scaling.baseStrength() * moodMultiplier * scaling.moodMultiplier();
                LOGGER.info("Wind Charge Radius: {}, Strength: {}", radius, strength);
                projectile = new MoodyWindChargeEntity(level, player, movement, radius, strength);
            }
            case FireChargeItem fireChargeItem -> {
                ProjectileScaling scaling = ProjectileScaling.FIRE;
                int explosionPower = Math.round(scaling.baseStrength() * moodMultiplier * scaling.moodMultiplier());
                LOGGER.info("FireBall Power: {}", explosionPower);
                projectile = new LargeFireball(level, player, movement, explosionPower);
            }
            case null, default -> {
                LOGGER.error("Invalid projectile item: {}", projectileItem);
                throw new IllegalArgumentException("Unknown projectile item: " + projectileItem);
            }
        }

        projectile.setPos(spawnPos);
        level.levelEvent(null, 1016, ghast.blockPosition(), 0);
        level.addFreshEntity(projectile);
    }

    // ============================================================
    // Mood Logic
    // ============================================================

    private void adjustMood(HappyGhast ghast, float delta) {
        float currentMood = ghast.getData(ModAttachments.MOOD);

        ParticleOptions particle = delta > 0F ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HAPPY_VILLAGER;
        if (wouldCrossMoodThreshold(currentMood, delta)) spawnSurroundParticles(ghast, particle);

        float newMood = Mth.clamp(currentMood + delta, GhastMoodMap.MIN, GhastMoodMap.MAX);
        LOGGER.info("Adjusting mood by {} from {} to {}", delta, currentMood, newMood);
        ghast.setData(ModAttachments.MOOD, newMood);
    }

    private boolean wouldCrossMoodThreshold(float currentMood, float delta) {
        float newMood = Mth.clamp(currentMood + delta, GhastMoodMap.MIN, GhastMoodMap.MAX);
        GhastMoodMap thresholds = GhastMoodMap.get();

        return !thresholds.getMoodFromValue(currentMood).equals(thresholds.getMoodFromValue(newMood));
    }

    // ============================================================
    // Particle Effects
    // ============================================================

    private void spawnSurroundParticles(HappyGhast ghast, ParticleOptions particleOption) {
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

    private void spawnMouthParticles(HappyGhast ghast, ParticleOptions particleOption) {
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

    // ============================================================
    // Utility
    // ============================================================

    private Vec3 getProjectileSpawnPos(HappyGhast ghast) {
        Vec3 viewVec = ghast.getViewVector(1.0F);  // Get ghast's view direction
        return new Vec3(
                ghast.getX() + viewVec.x * 4.0,  // Offset X by 4 blocks in ghast's facing direction
                ghast.getEyeY(),                  // Vertical position at ghast's eye level
                ghast.getZ() + viewVec.z * 4.0    // Offset Z by 4 blocks in ghast's facing direction
        );
    }

    private Vec3 getPlayerAimVector(Player player) {
        //TODO: Consider remove/change the - 4 offset
        float clampedPitch = Mth.clamp(player.getXRot() - 4, -60f, 60f);
        return Vec3.directionFromRotation(clampedPitch, player.getYRot());
    }

    //TODO: Still does not work correctly with bucket items
    private void consumePlayerItem(Player player, ItemStack stack) {
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (player.getAbilities().instabuild) return; // creative mode, no change

        Item item = stack.getItem();
        ItemStack result;

        if (item instanceof BucketItem) {
            // Get the empty bucket BEFORE shrinking
            result = BucketItem.getEmptySuccessItem(stack, player);
        } else {
            result = item.getCraftingRemainder(stack);
        }

        stack.shrink(1);

        // Give the player the resulting item (empty bucket or remainder)
        if (!result.isEmpty()) {
            if (!player.getInventory().add(result)) {
                player.drop(result, false); // drop if inventory full
            }
        }
    }

    private void applySharedCooldown(Player player, Item projectileItem) {
        ItemCooldowns cooldownTracker = player.getCooldowns();

        //Applies the cooldown of the used projectileItem to all HAPPY_GHAST_PROJECTILES in inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ModTags.Items.HAPPY_GHAST_PROJECTILES)) {
                //TODO: Get Item specific cooldown from moody_projectiles_tag.json
                cooldownTracker.addCooldown(stack, 40);
            }
        }
    }

    private boolean isBusy(HappyGhast ghast){
        return ghast.getData(ModAttachments.IS_CHARGING)
                || ghast.getData(ModAttachments.IS_SNOWBALL_BARRAGE)
                || ghast.getData(ModAttachments.IS_CONSUMING_FOOD);
    }

    public record ProjectileScaling(float baseRadius, float baseStrength, float moodMultiplier) {
        // Ice Charge
        public static final ProjectileScaling ICE = new ProjectileScaling(
                4.5F,   // baseRadius
                4.0F,   // baseStrength
                0.7F    // moodMultiplier
        );

        // Wind Charge
        public static final ProjectileScaling WIND = new ProjectileScaling(
                1.8F,   // baseRadius (vanilla is 1.2)
                1.8F,   // baseStrength (vanilla is 1.22)
                0.9F    // moodMultiplier
        );

        // Fire Charge
        public static final ProjectileScaling FIRE = new ProjectileScaling(
                1.5F,   // baseRadius (vanilla is 1.0)
                1.5F,   // baseStrength (vanilla is 1.0)
                1.0F    // moodMultiplier
        );
    }

}