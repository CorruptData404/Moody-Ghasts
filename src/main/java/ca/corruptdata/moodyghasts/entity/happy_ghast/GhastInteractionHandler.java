package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.registry.ModTags;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern_factories.FiringPatternFactory;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import ca.corruptdata.moodyghasts.registry.ModRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public class GhastInteractionHandler {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;

    private final GhastShootingHandler shootingHandler;

    public GhastInteractionHandler(GhastShootingHandler shootingHandler) {
        this.shootingHandler = shootingHandler;
    }

    @SubscribeEvent
    private void onRiderUseProjectile(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModTags.Items.MOODY_PROJECTILES)) return;
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player.getVehicle() instanceof HappyGhast ghast)) return;
        if (player != ghast.getControllingPassenger()) return;
        event.setCanceled(true);
        if (isBusy(ghast)) return;
        ItemPropertyMap.MoodyProjectile projectileData = stack.getItem()
                .builtInRegistryHolder()
                .getData(ItemPropertyMap.MoodyProjectile.DATA_MAP);
        if (projectileData == null) return;
        if (stack.getCount() < projectileData.count()) return; // count 0 = always usable, never consumed


        Registry<ProjectileFactory> factoryRegistry = Objects.requireNonNull(player.level().getServer())
                .registryAccess()
                .lookupOrThrow(ModRegistries.PROJECTILE_FACTORIES);

        Registry<FiringPatternFactory> behaviourRegistry = Objects.requireNonNull(player.level().getServer())
                .registryAccess()
                .lookupOrThrow(ModRegistries.FIRING_PATTERN_FACTORIES);

        shootingHandler.startShooting(ghast, player, stack, factoryRegistry, behaviourRegistry);
        applyCooldownToProjectiles(player, projectileData.cooldown());
        consumePlayerItem(player, event.getHand(), stack, projectileData.count(), projectileData.remainderItem());
    }

    @SubscribeEvent
    private void onRiderFeed(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemTags.HAPPY_GHAST_FOOD)) return;
        if (!(event.getEntity().getVehicle() instanceof HappyGhast ghast)) return;
        if (event.getEntity() != ghast.getControllingPassenger()) return;
        event.setCanceled(true);
        if (isBusy(ghast)) return;
        ItemPropertyMap.MoodyConsumable consumableData = stack.getItem()
                .builtInRegistryHolder()
                .getData(ItemPropertyMap.MoodyConsumable.DATA_MAP);
        if (consumableData == null) return;
        if (stack.getCount() < consumableData.count()) return; // count 0 = always usable, never consumed

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        consumePlayerItem(event.getEntity(), event.getHand(), stack, consumableData.count(), consumableData.remainderItem());

    }

    @SubscribeEvent
    private void onInteractFeed(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemTags.HAPPY_GHAST_FOOD)) return;
        if (!(event.getTarget() instanceof HappyGhast ghast)) return;
        if (ghast.isBaby()) return;
        event.setCanceled(true);
        if (isBusy(ghast)) return;
        ItemPropertyMap.MoodyConsumable consumableData = stack.getItem()
                .builtInRegistryHolder()
                .getData(ItemPropertyMap.MoodyConsumable.DATA_MAP);
        if (consumableData == null) return;
        if (stack.getCount() < consumableData.count()) return; // count 0 = always usable, never consumed

        event.setCancellationResult(InteractionResult.SUCCESS);

        ghast.setData(ModAttachments.IS_CONSUMING_FOOD, true);
        ghast.setData(ModAttachments.CURRENT_FOOD, stack.getItem());

        consumePlayerItem(event.getEntity(), event.getHand(), stack, consumableData.count(), consumableData.remainderItem());
    }

    private boolean isBusy(HappyGhast ghast){
        return ghast.getData(ModAttachments.IS_CHARGING)
                || ghast.getData(ModAttachments.IS_FIRING)
                || ghast.getData(ModAttachments.IS_CONSUMING_FOOD)
                || shootingHandler.isActive(ghast);
    }

    private void consumePlayerItem(Player player, InteractionHand hand,
                                   ItemStack stack, int count, Optional<Identifier> remainderItem) {

        player.awardStat(Stats.ITEM_USED.get(stack.getItem()), Math.max(count, 1));

        if (player.getAbilities().instabuild) return;

        ItemStack remainder;
        if (remainderItem.isPresent()) {
            Item item = BuiltInRegistries.ITEM.getOptional(remainderItem.get()).orElse(null);
            if (item == null) {
                LOGGER.warn("Configured remainderItem '{}' for '{}' does not exist - no remainder will be " +
                                "given. Check the moody_projectiles_map/moody_consumables_map entry.",
                        remainderItem.get(), BuiltInRegistries.ITEM.getKey(stack.getItem()));
                remainder = ItemStack.EMPTY;
            } else {
                remainder = new ItemStack(item, count);
            }
        } else if (stack.has(DataComponents.USE_REMAINDER)) {
            remainder = stack.get(DataComponents.USE_REMAINDER).convertInto().create();
            if (!remainder.isEmpty()) remainder = remainder.copyWithCount(count);
        } else if (stack.getItem() instanceof BucketItem) {
            remainder = BucketItem.getEmptySuccessItem(stack, player);
            if (!remainder.isEmpty()) remainder = remainder.copyWithCount(count);
        } else {
            remainder = ItemStack.EMPTY;
        }

        ItemStack shrunk = stack.copyWithCount(Math.max(0, stack.getCount() - count));

        if (remainder.isEmpty()) {
            player.setItemInHand(hand, shrunk);
            return;
        }
        if (shrunk.isEmpty()) {
            player.setItemInHand(hand, remainder);
            return;
        }
        player.setItemInHand(hand, shrunk);
        if (!player.getInventory().add(remainder)) {
            player.drop(remainder, false);
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