package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.registry.ModTags;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour.ShootingBehaviourFactory;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import ca.corruptdata.moodyghasts.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.util.Objects;

public class GhastInteractionHandler {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;

    private final GhastShootingHandler shootingHandler;

    public GhastInteractionHandler(GhastShootingHandler shootingHandler) {
        this.shootingHandler = shootingHandler;
    }

    @SubscribeEvent
    private void onRiderUseProjectile(PlayerInteractEvent.RightClickItem event) {
        ItemStack projectileItem = event.getItemStack();
        if (!projectileItem.is(ModTags.Items.MOODY_PROJECTILES)) return;
        Player player = event.getEntity();
        if (!(player.getVehicle() instanceof HappyGhast ghast)) return;
        if (player != ghast.getControllingPassenger()) return;
        event.setCanceled(true);
        if (player.level().isClientSide()) return;
        if (isBusy(ghast)) return;

        Registry<GhastProjectileFactory> factoryRegistry = Objects.requireNonNull(player.level().getServer())
                .registryAccess()
                .lookupOrThrow(ModRegistries.PROJECTILE_FACTORIES);

        Registry<ShootingBehaviourFactory> behaviourRegistry = Objects.requireNonNull(player.level().getServer())
                .registryAccess()
                .lookupOrThrow(ModRegistries.SHOOTING_BEHAVIOURS);

        shootingHandler.startShooting(ghast, player, projectileItem, factoryRegistry, behaviourRegistry);
        applyCooldownToProjectiles(player,
                Objects.requireNonNull(projectileItem.getItem()
                                .builtInRegistryHolder()
                                .getData(ItemPropertyMap.MoodyProjectile.DATA_MAP))
                        .cooldown());
        consumePlayerItem(player, event.getHand(), projectileItem);
    }

    @SubscribeEvent
    private void onRiderFeed(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemTags.HAPPY_GHAST_FOOD)) return;
        if (!(event.getEntity().getVehicle() instanceof HappyGhast ghast)) return;
        if (event.getEntity() != ghast.getControllingPassenger()) return;
        if(stack.getItem().builtInRegistryHolder().getData(ItemPropertyMap.MoodyConsumable.DATA_MAP) == null) return;
        event.setCanceled(true);
        if (isBusy(ghast)) return;

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        consumePlayerItem(event.getEntity(), event.getHand(), stack);

    }

    @SubscribeEvent
    private void onInteractFeed(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemTags.HAPPY_GHAST_FOOD)) return;
        if (!(event.getTarget() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        if (isBusy(ghast)) return;
        if(stack.getItem().builtInRegistryHolder().getData(ItemPropertyMap.MoodyConsumable.DATA_MAP) == null) return;

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        consumePlayerItem(event.getEntity(), event.getHand(), stack);
    }

    private boolean isBusy(HappyGhast ghast){
        return ghast.getData(ModAttachments.IS_CHARGING)
                || ghast.getData(ModAttachments.IS_BARRAGING)
                || ghast.getData(ModAttachments.IS_CONSUMING_FOOD)
                || shootingHandler.isActive(ghast);
    }

    private void consumePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (player.getAbilities().instabuild) return;

        ItemStack remainder;
        if (stack.has(DataComponents.USE_REMAINDER)) {
            remainder = stack.get(DataComponents.USE_REMAINDER).convertInto().create();
        } else if (stack.getItem() instanceof BucketItem) {
            remainder = BucketItem.getEmptySuccessItem(stack, player);
        } else if (stack.is(Tags.Items.BUCKETS)) {
            remainder = new ItemStack(Items.BUCKET);
        } else {
            remainder = ItemStack.EMPTY;
        }

        ItemStack result = ItemUtils.createFilledResult(stack, player, remainder);
        player.setItemInHand(hand, result);
    }

    private void applyCooldownToProjectiles(Player player, int cooldown) {
        ItemCooldowns cooldownTracker = player.getCooldowns();

        //Applies the cooldown of the used projectileItem to all HAPPY_GHAST_PROJECTILES in inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ModTags.Items.MOODY_PROJECTILES)) {
                cooldownTracker.addCooldown(stack, cooldown);
            }
        }
    }
}