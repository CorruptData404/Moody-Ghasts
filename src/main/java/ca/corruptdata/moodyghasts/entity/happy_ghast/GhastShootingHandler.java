package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour.ShootingBehaviour;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.behaviour.ShootingBehaviourFactory;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.GhastProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhastShootingHandler {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;

    // Transient runtime state — not serialized, intentionally lost on restart
    private final Map<UUID, ShootingBehaviour> activeBehaviours = new HashMap<>();

    // ============================================================
    // Public API — called by GhastInteractionHandler
    // ============================================================

    public boolean isActive(HappyGhast ghast) {
        return activeBehaviours.containsKey(ghast.getUUID());
    }

    public void startShooting(HappyGhast ghast, Player player, ItemStack projectileItem,
                              Registry<GhastProjectileFactory> factoryRegistry,
                              Registry<ShootingBehaviourFactory> behaviourRegistry) {
        var projectileData = projectileItem.getItem()
                .builtInRegistryHolder()
                .getData(ItemPropertyMap.MoodyProjectile.DATA_MAP);

        if (projectileData == null) {
            LOGGER.error("No projectile data found for item: {}", projectileItem);
            return;
        }

        GhastProjectileFactory projFactory = factoryRegistry.get(projectileData.projectile().type())
                .map(Holder.Reference::value)
                .orElse(null);

        if (projFactory == null) {
            LOGGER.error("No factory registered for projectile type: {}",
                    projectileData.projectile().type());
            return;
        }

        ShootingBehaviourFactory behaviourFactory = behaviourRegistry.get(projectileData.shot().type())
                .map(Holder.Reference::value)
                .orElse(null);

        if (behaviourFactory == null) {
            LOGGER.error("No behaviour registered for shot type: {}",
                    projectileData.shot().type());
            return;
        }

        ShootingBehaviour behaviour = behaviourFactory.create(
                projFactory,
                ghast,
                player,
                projectileData,
                ghast.getData(ModAttachments.MOOD));

        activeBehaviours.put(ghast.getUUID(), behaviour);
    }

    // ============================================================
    // Event Handlers
    // ============================================================

    @SubscribeEvent
    private void onGhastTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide) return;
        if (ghast.isBaby()) return;

        ShootingBehaviour behaviour = activeBehaviours.get(ghast.getUUID());
        if (behaviour == null) return;

        behaviour.tick();

        // Clean up once both flags are clear — behaviour has fully finished
        if (!ghast.getData(ModAttachments.IS_CHARGING)
                && !ghast.getData(ModAttachments.IS_BARRAGING)) {
            activeBehaviours.remove(ghast.getUUID());
        }
    }

    @SubscribeEvent
    private void onServerStopping(ServerStoppingEvent event) {
        // Clear all active behaviours on server stop — clean slate on next start
        activeBehaviours.clear();
    }
}