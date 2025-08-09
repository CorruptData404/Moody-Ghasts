package ca.corruptdata.moodyghasts;

import ca.corruptdata.moodyghasts.entity.projectile.IceChargeEntity;
import ca.corruptdata.moodyghasts.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ModDispenserBehaviors {
    public static void register() {
        DispenserBlock.registerBehavior(ModItems.ICE_CHARGE.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Level level = source.level();
                Position position = DispenserBlock.getDispensePosition(source);
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                
                IceChargeEntity iceCharge = ModItems.ICE_CHARGE.get().asProjectile(
                    level,
                    position,
                    stack,
                    direction
                );
                
                level.addFreshEntity(iceCharge);
                stack.shrink(1);
                return stack;
            }

            @Override
            protected void playSound(BlockSource source) {
                source.level().levelEvent(1051, source.pos(), 0);
            }
        });
    }
}