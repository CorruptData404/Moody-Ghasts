package ca.corruptdata.moodyghasts.entity.happy_ghast;

import ca.corruptdata.moodyghasts.registry.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern.FiringPattern;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern_factories.FiringPatternFactory;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
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
    private final Map<UUID, FiringPattern> activePatterns = new HashMap<>();

    public boolean isActive(HappyGhast ghast) {
        return activePatterns.containsKey(ghast.getUUID());
    }

    public void startShooting(HappyGhast ghast, Player player, ItemStack projectileItem,
                              Registry<ProjectileFactory> projFactoryRegistry,
                              Registry<FiringPatternFactory> pattFactoryRegistry) {
        var projectileData = projectileItem.getItem()
                .builtInRegistryHolder()
                .getData(ItemPropertyMap.MoodyProjectile.DATA_MAP);

        if (projectileData == null) {
            LOGGER.error("No projectile data found for item: {}", projectileItem);
            return;
        }

        ProjectileFactory projFactory = projFactoryRegistry.get(projectileData.projectile().type())
                .map(Holder.Reference::value)
                .orElse(null);

        if (projFactory == null) {
            LOGGER.error("No factory registered for projectile type: {}",
                    projectileData.projectile().type());
            return;
        }

        FiringPatternFactory pattFactory = pattFactoryRegistry.get(projectileData.shot().type())
                .map(Holder.Reference::value)
                .orElse(null);

        if (pattFactory == null) {
            LOGGER.error("No factory registered for firing pattern type: {}",
                    projectileData.shot().type());
            return;
        }

        FiringPattern behaviour = pattFactory.createPattern(
                projFactory,
                ghast,
                player,
                projectileData,
                ghast.getData(ModAttachments.MOOD));

        activePatterns.put(ghast.getUUID(), behaviour);
    }

    @SubscribeEvent
    private void onGhastTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof HappyGhast ghast)) return;
        if (ghast.level().isClientSide()) return;
        if (ghast.isBaby()) return;

        FiringPattern behaviour = activePatterns.get(ghast.getUUID());
        if (behaviour == null) return;

        behaviour.tick();

        // Clean up once both flags are clear — pattern has fully finished
        if (!ghast.getData(ModAttachments.IS_CHARGING)
                && !ghast.getData(ModAttachments.IS_FIRING)) {
            activePatterns.remove(ghast.getUUID());
        }
    }

    @SubscribeEvent
    private void onServerStopping(ServerStoppingEvent event) {
        // Clear all active patterns on server stop — clean slate on next start
        activePatterns.clear();
    }
}