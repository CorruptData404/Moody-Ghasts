package ca.corruptdata.moodyghasts.entity;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.attachment.ModAttachments;
import ca.corruptdata.moodyghasts.component.ModDataComponentTypes;
import ca.corruptdata.moodyghasts.entity.projectile.GhastIceChargeEntity;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import ca.corruptdata.moodyghasts.util.ModTags;
import ca.corruptdata.moodyghasts.util.MoodThresholdsManager;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.InteractionResult;

public class HappyGhastHandler {
    private static final float MIN_MOOD = 0.0f;
    private static final float MAX_MOOD = 100.0f;
    //Should always be Negative
    private static final int HEALED_MOOD_MULTIPLIER = -2;
    //Should always be Positive
    private static final int DAMAGED_MOOD_MULTIPLIER = 2;

    @SubscribeEvent
    public void chargingShoot(EntityTickEvent.Post event) {

        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;
        if (!ghast.getData(ModAttachments.IS_CHARGING)) return;

        int chargeTime = ghast.getData(ModAttachments.CHARGE_TIME);
        Player player = (Player) ghast.getControllingPassenger();

        if(player == ghast.getData(ModAttachments.SHOOTING_PLAYER) && !(chargeTime > 20)){
            ghast.setData(ModAttachments.CHARGE_TIME, chargeTime + 1);
        }
        else
        {
            ghast.setData(ModAttachments.IS_CHARGING,false);
            ghast.setData(ModAttachments.CHARGE_TIME, 0);
        }

        if(chargeTime == 10){
            ghast.level().levelEvent(ghast, 1015, ghast.blockPosition(), 0);
        }
        else if(chargeTime == 20){
            shoot(player, ghast, ghast.getData(ModAttachments.PROJECTILE_ITEM));
            // TODO: Implement other projectile types
            ghast.setData(ModAttachments.IS_CHARGING,false);
            ghast.setData(ModAttachments.CHARGE_TIME, 0);
        }
    }


    @SubscribeEvent
    public void onRiderShoot(PlayerInteractEvent.RightClickItem event) {
        ItemStack projectileItem = event.getItemStack();
        if (!projectileItem.is(ModTags.Items.HAPPY_GHAST_PROJECTILES)) return;
        if (!(event.getEntity().getVehicle() instanceof HappyGhast ghast)) return;
        if (event.getEntity() != ghast.getControllingPassenger()) return;

        if (!ghast.getData(ModAttachments.IS_CHARGING)) { // Only start if not already charging
            ghast.setData(ModAttachments.IS_CHARGING,true);
            ghast.setData(ModAttachments.CHARGE_TIME, 1); // Start charging
            ghast.setData(ModAttachments.PROJECTILE_ITEM, projectileItem.getItem());
            ghast.setData(ModAttachments.SHOOTING_PLAYER, event.getEntity());
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRiderFeed(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.has(ModDataComponentTypes.MOOD_DELTA)) return;
        if (!(event.getEntity().getVehicle() instanceof HappyGhast ghast)) return;
        if (event.getEntity() != ghast.getControllingPassenger()) return;
        
        event.setCanceled(true);
        handleFeed(ghast, stack);
        stack.consume(1, event.getEntity());

    }

    @SubscribeEvent
    public void onInteractFeed(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.has(ModDataComponentTypes.MOOD_DELTA)) return;
        if (!(event.getTarget() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        
        handleFeed(ghast, stack);
        stack.consume(1, event.getEntity());
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getAmount() * HEALED_MOOD_MULTIPLIER);
    }
    @SubscribeEvent
    public void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        adjustMood(ghast, event.getNewDamage() * DAMAGED_MOOD_MULTIPLIER);
    }

    private void shoot(Player player, HappyGhast ghast, Item projectileItem) {
        applySharedCooldown(player,projectileItem);
        if (projectileItem instanceof IceChargeItem
                || projectileItem instanceof WindChargeItem
                || projectileItem instanceof FireChargeItem) {
            shootCharge(player, ghast, projectileItem);
        }

    }

    private void shootCharge(Player player, HappyGhast ghast, Item projectileItem) {
        Vec3 spawnPos = calculateSpawnPosition(ghast);
        Vec3 movement = calculateMovementVector(player);
        AbstractHurtingProjectile projectile = null;

        if (projectileItem instanceof IceChargeItem) {
            projectile = new GhastIceChargeEntity(player, ghast.level(), movement, ghast.getData(ModAttachments.MOOD));
        }

        assert projectile != null;
        projectile.setPos(spawnPos);
        ghast.level().levelEvent(null, 1016, ghast.blockPosition(), 0);
        ghast.level().addFreshEntity(projectile);
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

    //TODO: Make eating take time like a player
    private void handleFeed(HappyGhast ghast, ItemStack treat) {
        float moodDelta = treat.getOrDefault(ModDataComponentTypes.MOOD_DELTA, 0F);
        float currentMood = ghast.getData(ModAttachments.MOOD);
        
        if (wouldCrossMoodThreshold(currentMood, moodDelta)) {
            addParticlesAroundSelf(ghast, moodDelta > 0 ?
                    ParticleTypes.ANGRY_VILLAGER :
                    ParticleTypes.HAPPY_VILLAGER);
        }

        adjustMood(ghast, moodDelta);
        
        ghast.level().playSound(null, ghast.getX(), ghast.getY(), ghast.getZ(),
                SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 1.0F, 1.0F);

        //TODO: Change to AroundMouth once implemented
        addParticlesAroundSelf(ghast, new ItemParticleOption(ParticleTypes.ITEM, treat));
    }

    private void adjustMood(HappyGhast ghast, float delta) {
        float currentMood = ghast.getData(ModAttachments.MOOD);
        if (wouldCrossMoodThreshold(currentMood, delta)) {
            addParticlesAroundSelf(ghast, delta > 0 ?
                    ParticleTypes.ANGRY_VILLAGER :
                    ParticleTypes.HAPPY_VILLAGER);
        }
        MoodyGhasts.LOGGER.info("Adjusting mood by {} from {} to {}", delta, currentMood, Mth.clamp(currentMood + delta, MIN_MOOD, MAX_MOOD));
        ghast.setData(ModAttachments.MOOD, Mth.clamp(currentMood + delta, MIN_MOOD, MAX_MOOD));
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
        return MoodThresholdsManager.getCurrentInstance().getMoodMap().values().stream()
                .anyMatch(threshold ->
                        (currentMood <= threshold && newMood > threshold) ||
                        (currentMood > threshold && newMood <= threshold));
    }

    private void addParticlesAroundSelf(HappyGhast ghast, ParticleOptions particleOption) {
        for (int i = 0; i < 10; i++) {
            double d0 = ghast.getRandom().nextGaussian() * 0.02;
            double d1 = ghast.getRandom().nextGaussian() * 0.02;
            double d2 = ghast.getRandom().nextGaussian() * 0.02;
            ghast.level().addParticle(particleOption, ghast.getRandomX(2.0), ghast.getRandomY() + 1.0, ghast.getRandomZ(1.0), d0, d1, d2);
        }
    }

    private void addParticlesAroundMouth(HappyGhast ghast, ParticleOptions particleOption) {
        // TODO: Implement mouth-specific particle spawning
    }
}