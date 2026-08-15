package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern_factories;

import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern.Barrage;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern.FiringPattern;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public class BarrageFactory implements FiringPatternFactory {

    @Override
    public FiringPattern buildPattern(ProjectileFactory factory, HappyGhast ghast,
                                       Player player, ItemPropertyMap.MoodyProjectile data, float mood) {

        return new Barrage(factory, ghast, player, data, mood);
    }

    @Override
    public Set<String> getRecognizedMoodScalingKeys() {
        return Set.of("count", "velocity", "inaccuracy");
    }
}
