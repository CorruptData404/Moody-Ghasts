package ca.corruptdata.moodyghasts.entity;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.component.ModDataComponentTypes;
import ca.corruptdata.moodyghasts.entity.projectile.MoodyIceChargeEntity;
import ca.corruptdata.moodyghasts.entity.projectile.MoodyWindChargeEntity;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import ca.corruptdata.moodyghasts.ModTags;
import ca.corruptdata.moodyghasts.moodutil.Mood;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholds;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholdsManager;
import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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

public class HappyGhastHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final float MIN_MOOD = 0.0f;
    private static final float MAX_MOOD = 100.0f;
    private static final int BASE_SNOWBALL_COUNT = 50;
    //Should always be Negative
    private static final float HEALED_MOOD_MULTIPLIER = -2f;
    //Should always be Positive
    private static final float DAMAGED_MOOD_MULTIPLIER = 2f;


    @SubscribeEvent
    private void onRiderShoot(PlayerInteractEvent.RightClickItem event) {
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

        handleItemConsumption(event.getEntity(), projectileItem);
    }

    @SubscribeEvent
    private void chargingShoot(EntityTickEvent.Post event) {

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
    private void onRiderFeed(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.has(ModDataComponentTypes.MOOD_DELTA)) return;
        if (!(event.getEntity().getVehicle() instanceof HappyGhast ghast)) return;
        if (event.getEntity() != ghast.getControllingPassenger()) return;
        event.setCanceled(true);
        if (isBusy(ghast)) return;

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        handleItemConsumption(event.getEntity(), stack);

    }

    @SubscribeEvent
    private void onInteractFeed(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.has(ModDataComponentTypes.MOOD_DELTA)) return;
        if (!(event.getTarget() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        if (isBusy(ghast)) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        handleItemConsumption(event.getEntity(), stack);
    }

    @SubscribeEvent
    private void tickEating(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;
        if (!ghast.getData(ModAttachments.IS_CONSUMING_FOOD)) return;

        int consumeTime = ghast.getData(ModAttachments.FOOD_CONSUME_TICKS);
        ghast.setData(ModAttachments.FOOD_CONSUME_TICKS, consumeTime + 1);

        // Every 4 ticks, play eating sound + particles
        if (consumeTime % 4 == 0) {
            ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F, 1.0F);

            addParticlesAroundSelf(ghast, new ItemParticleOption(
                    ParticleTypes.ITEM,
                    ghast.getData(ModAttachments.CURRENT_FOOD).getDefaultInstance()
            ));
        }

        // Finish eating
        if (consumeTime >= 32) {
            float moodDelta = ghast.getData(ModAttachments.CURRENT_FOOD)
                    .getDefaultInstance()
                    .getOrDefault(ModDataComponentTypes.MOOD_DELTA, 0F);

            adjustMood(ghast, moodDelta);

            ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 1.0F, 1.0F);

            ghast.setData(ModAttachments.IS_CONSUMING_FOOD, false);
            ghast.setData(ModAttachments.CURRENT_FOOD, Items.AIR);
            ghast.setData(ModAttachments.FOOD_CONSUME_TICKS, 0);
        }
    }

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

    private void shoot(Player player, HappyGhast ghast, Item projectileItem) {
        applySharedCooldown(player,projectileItem);
        float mood = ghast.getData(ModAttachments.MOOD);
        float moodMultiplier = getMoodMultiplier(mood);

        if (projectileItem instanceof IceChargeItem
                || projectileItem instanceof WindChargeItem
                || projectileItem instanceof FireChargeItem) {
            shootCharge(player, ghast, projectileItem, moodMultiplier);
        } else if (projectileItem == Items.POWDER_SNOW_BUCKET) {
            shootBarrage(ghast, moodMultiplier);
        }
    }

    private static float getMoodMultiplier(float mood) {
        MoodThresholds thresholds = MoodThresholdsManager.getCurrentInstance();
        float[] multipliers = {0.6F, 0.8F, 1.0F, 1.3F, 1.6F, 2.0F}; // ordered to match Mood.values()

        Mood[] moods = Mood.values();
        for (int i = 0; i < moods.length; i++) {
            if (mood <= thresholds.get(moods[i])) {
                return multipliers[i];
            }
        }
        return 2.0F; // fallback if above all thresholds
    }

    private void shootCharge(Player player, HappyGhast ghast, Item projectileItem, float moodMultiplier) {
        Level level = ghast.level();
        Vec3 spawnPos = calculateSpawnPosition(ghast);
        Vec3 movement = calculateMovementVector(player);
        AbstractHurtingProjectile projectile;

        if (projectileItem instanceof IceChargeItem) {
            ProjectileScaling scaling = ProjectileScaling.ICE;
            int radius = (int) (scaling.baseRadius() * moodMultiplier * scaling.moodMultiplier());
            float strength = scaling.baseStrength() * moodMultiplier * scaling.moodMultiplier();
            LOGGER.info("Ice Charge Radius: {}, Strength: {}", radius, strength);
            projectile = new MoodyIceChargeEntity(level, player, movement, radius, strength);
        }
        else if (projectileItem instanceof WindChargeItem) {
            ProjectileScaling scaling = ProjectileScaling.WIND;
            float radius = scaling.baseRadius() * moodMultiplier * scaling.moodMultiplier();
            float strength = scaling.baseStrength() * moodMultiplier * scaling.moodMultiplier();
            LOGGER.info("Wind Charge Radius: {}, Strength: {}", radius, strength);
            projectile = new MoodyWindChargeEntity(level, player, movement, radius, strength);
        }
        else if (projectileItem instanceof FireChargeItem) {
            ProjectileScaling scaling = ProjectileScaling.FIRE;
            int explosionPower = Math.round(scaling.baseStrength() * moodMultiplier * scaling.moodMultiplier());
            LOGGER.info("FireBall Power: {}", explosionPower);
            projectile = new LargeFireball(level, player, movement, explosionPower);
        }
        else {
            LOGGER.error("Invalid projectile item: {}", projectileItem);
            throw new IllegalArgumentException("Unknown projectile item: " + projectileItem);
        }

        projectile.setPos(spawnPos);
        level.levelEvent(null, 1016, ghast.blockPosition(), 0);
        level.addFreshEntity(projectile);
    }

    private void shootBarrage(HappyGhast ghast, float moodMultiplier) {
        int totalSnowballs = (int) (BASE_SNOWBALL_COUNT * moodMultiplier);

        ghast.setData(ModAttachments.SNOWBALLS_LEFT, totalSnowballs);
        ghast.setData(ModAttachments.IS_SNOWBALL_BARRAGE, true);
        ghast.setData(ModAttachments.SNOWBALL_COOLDOWN, 0);
    }

    @SubscribeEvent
    private void onBarrageTick(EntityTickEvent.Post event) {
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
        Vec3 spawnPos = calculateSpawnPosition(ghast);

        // Create snowball
        Snowball snowball = new Snowball(ghast.level(), spawnPos.x(), spawnPos.y(), spawnPos.z(),
                new ItemStack(Items.SNOWBALL));
        snowball.setOwner(player);

        // Calculate base direction vector
        Vec3 direction = calculateMovementVector(player);

        float spread = 0.11485f;
        RandomSource random = ghast.getRandom();
        Vec3 spreadVector = new Vec3(
                random.triangle(direction.x, spread),
                random.triangle(direction.y, spread),
                random.triangle(direction.z, spread)
        );

        // Calculate speed based on progress (faster at start, slower at end)
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


    private Vec3 calculateSpawnPosition(HappyGhast ghast) {
        Vec3 viewVec = ghast.getViewVector(1.0F);  // Get ghast's view direction
        return new Vec3(
                ghast.getX() + viewVec.x * 4.0,  // Offset X by 4 blocks in ghast's facing direction
                ghast.getEyeY(),                  // Vertical position at ghast's eye level
                ghast.getZ() + viewVec.z * 4.0    // Offset Z by 4 blocks in ghast's facing direction
        );
    }

    private Vec3 calculateMovementVector(Player player) {
        //TODO: Consider remove/change the - 4 offset
        float clampedPitch = Mth.clamp(player.getXRot() - 4, -60f, 60f);
        return Vec3.directionFromRotation(clampedPitch, player.getYRot());
    }


    private void adjustMood(HappyGhast ghast, float delta) {
        float currentMood = ghast.getData(ModAttachments.MOOD);

        ParticleOptions particle = delta > 0F ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HAPPY_VILLAGER;
        if (wouldCrossMoodThreshold(currentMood, delta)) addParticlesAroundSelf(ghast, particle);

        float newMood = Mth.clamp(currentMood + delta, MIN_MOOD, MAX_MOOD);
        LOGGER.info("Adjusting mood by {} from {} to {}", delta, currentMood, newMood);
        ghast.setData(ModAttachments.MOOD, newMood);
    }

    //TODO: Still does not work correctly with bucket items
    private void handleItemConsumption(Player player, ItemStack stack) {
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
                //TODO: Get Item specific cooldown from happy_ghast_projectiles.json
                cooldownTracker.addCooldown(stack, 40);
            }
        }
    }

    private boolean wouldCrossMoodThreshold(float currentMood, float delta) {
        float newMood = Mth.clamp(currentMood + delta, MIN_MOOD, MAX_MOOD);
        MoodThresholds thresholds = MoodThresholdsManager.getCurrentInstance();

        for (Mood mood : Mood.values()) {
            float threshold = thresholds.get(mood);
            if ((currentMood <= threshold && newMood > threshold) ||
                    (currentMood > threshold && newMood <= threshold)) {
                return true;
            }
        }
        return false;
    }

    private void addParticlesAroundSelf(HappyGhast ghast, ParticleOptions particleOption) {
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

    private void addParticlesAroundMouth(HappyGhast ghast, ParticleOptions particleOption) {
        // TODO: Implement mouth-specific particle spawning
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