package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.ModTags;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour.ShootingBehaviourFactory;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import ca.corruptdata.moodyghasts.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
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
        if (player.level().isClientSide) return;
        if (isBusy(ghast)) return;

        Registry<GhastProjectileFactory> factoryRegistry = Objects.requireNonNull(player.getServer())
                .registryAccess()
                .lookupOrThrow(ModRegistries.PROJECTILE_FACTORIES);

        Registry<ShootingBehaviourFactory> behaviourRegistry = Objects.requireNonNull(player.getServer())
                .registryAccess()
                .lookupOrThrow(ModRegistries.SHOOTING_BEHAVIOURS);

        shootingHandler.startShooting(ghast, player, projectileItem, factoryRegistry, behaviourRegistry);
        applyCooldownToProjectiles(player,
                Objects.requireNonNull(projectileItem.getItem()
                                .builtInRegistryHolder()
                                .getData(ItemPropertyMap.MoodyProjectile.DATA_MAP))
                        .cooldown());
        consumePlayerItem(player, projectileItem);
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

        consumePlayerItem(event.getEntity(), stack);

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

        consumePlayerItem(event.getEntity(), stack);
    }

    private boolean isBusy(HappyGhast ghast){
        return ghast.getData(ModAttachments.IS_CHARGING)
                || ghast.getData(ModAttachments.IS_BARRAGING)
                || ghast.getData(ModAttachments.IS_CONSUMING_FOOD)
                || shootingHandler.isActive(ghast);
    }

    private void consumePlayerItem(Player player, ItemStack stack) {
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (player.getAbilities().instabuild) return;

        ItemStack remainder = stack.has(DataComponents.USE_REMAINDER)
                ? stack.get(DataComponents.USE_REMAINDER).convertInto()
                : ItemStack.EMPTY;

        stack.shrink(1);

        if (!remainder.isEmpty()) {
            if (stack.isEmpty()) {
                // Slot is now empty — place remainder directly into the hand
                player.setItemInHand(player.getUsedItemHand(), remainder);
            } else if (!player.getInventory().add(remainder)) {
                player.drop(remainder, false);
            }
        }
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
