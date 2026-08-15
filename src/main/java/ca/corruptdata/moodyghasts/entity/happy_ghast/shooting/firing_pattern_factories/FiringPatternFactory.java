package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern_factories;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern.FiringPattern;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.Set;
import java.util.TreeSet;

@FunctionalInterface
public interface FiringPatternFactory {

    Logger LOGGER = MoodyGhasts.LOGGER;

    /**
     * Creates the FiringPattern, logging the resolved value of every key in
     * {@link #getRecognizedMoodScalingKeys()} first (when {@code Config.SHOOT_LOGGING} is
     * on). Implementations should override {@link #buildPattern} rather than this method
     * directly, so logging can't accidentally be skipped
     * <p>
     * Note this only captures the BASE mood-scaled shot values, resolved once up front.
     * A pattern that re-derives a value per-shot from something that changes during firing
     * (e.g. Barrage scaling velocity by remaining progress) will still want its own
     * per-shot logging for that
     */
    default FiringPattern createPattern(ProjectileFactory factory, HappyGhast ghast,
                                        Player player, ItemPropertyMap.MoodyProjectile data, float mood) {
        if (Config.SHOOT_LOGGING.get()) {
            Set<String> keys = getRecognizedMoodScalingKeys();
            if (!keys.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String key : new TreeSet<>(keys)) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(key).append('=').append(data.shot().getScaled(key, mood));
                }
                LOGGER.info("Starting shot '{}' for ghast {} ({})", data.shot().type(), ghast.getUUID(), sb);
            }
        }

        return buildPattern(factory, ghast, player, data, mood);
    }

    /**
     * Builds the actual FiringPattern instance. Implementations override this instead of
     * {@link #createPattern} so the base logging above always runs.
     */
    FiringPattern buildPattern(ProjectileFactory factory, HappyGhast ghast,
                               Player player, ItemPropertyMap.MoodyProjectile data, float mood);

    /**
     * The {@code shot.moodScaling} keys patterns built by this factory actually read
     * (e.g. {@code Set.of("velocity", "inaccuracy")}). Override this so datapack validation
     * can catch typos/omissions in moody_projectiles_map.json for your shot type, and so
     * {@link #createPattern} can log the resolved values automatically.
     * <p>
     * Defaults to empty (no validation, no logging).
     */
    default Set<String> getRecognizedMoodScalingKeys() {
        return Set.of();
    }
}