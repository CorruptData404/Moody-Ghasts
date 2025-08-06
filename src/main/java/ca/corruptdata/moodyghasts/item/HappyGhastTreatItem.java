package ca.corruptdata.moodyghasts.item;


import ca.corruptdata.moodyghasts.api.HappyGhastTreat;
import ca.corruptdata.moodyghasts.component.ModDataComponentTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class HappyGhastTreatItem extends Item implements HappyGhastTreat {
    public HappyGhastTreatItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return HappyGhastTreat.super.use(level, player, hand);
    }
}
