package ca.corruptdata.moodyghasts.api;

import ca.corruptdata.moodyghasts.component.ModDataComponentTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface HappyGhastTreat {

    default InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (player.getVehicle() instanceof HappyGhast ghast && ghast.getControllingPassenger() == player) {
                InteractionResult result = ((HappyGhastAccessor)ghast).moodyghasts$feed(stack);
                if (result == InteractionResult.SUCCESS)
                {
                    stack.consume(1, player);
                }
                return result;
        }
        
        return InteractionResult.PASS;
    }
}