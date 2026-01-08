package ca.corruptdata.moodyghasts.moodutil;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class MoodThresholdsManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<Registry<MoodThresholds>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "mood_thresholds")
    );


    private static MoodThresholds currentThresholds = MoodThresholds.DEFAULT;

    public static MoodThresholds getCurrentInstance() {
        return currentThresholds;
    }

    public static void updateThresholds(RegistryAccess registryAccess) {
        try {
            var registry = registryAccess.lookupOrThrow(REGISTRY_KEY);
            currentThresholds = registry.getValue(MoodThresholds.ID);
            if (currentThresholds == null) {
                LOGGER.warn("No mood thresholds found in registry, using defaults");
                currentThresholds = MoodThresholds.DEFAULT;
            } else {
                LOGGER.info("Successfully loaded mood thresholds: {}", currentThresholds.getMap());
            }
        } catch (Exception e) {
            currentThresholds = MoodThresholds.DEFAULT;
            LOGGER.error("Failed to load mood thresholds, using defaults", e);
        }

    }
}
