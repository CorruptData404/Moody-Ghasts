package ca.corruptdata.moodyghasts.moodutil;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

public class MoodThresholdsManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<Registry<MoodThresholds>> REGISTRY_KEY = ResourceKey.createRegistryKey(MoodThresholds.ID);

    private static MoodThresholds currentThresholds = MoodThresholds.DEFAULT;

    public static MoodThresholds getCurrentInstance() {
        return currentThresholds;
    }

    public static void updateThresholds(RegistryAccess registryAccess) {
        var registry = registryAccess.lookupOrThrow(REGISTRY_KEY);
        currentThresholds = registry.getValue(MoodThresholds.ID);
        if (currentThresholds == null) {
            LOGGER.warn("No mood thresholds found in registry, using defaults");
            currentThresholds = MoodThresholds.DEFAULT;
        }
        LOGGER.info("Loaded mood thresholds: {}", currentThresholds.getMap());
    }
}
