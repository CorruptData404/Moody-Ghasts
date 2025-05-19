package ca.corruptdata.moodyghasts.item;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MoodyGhasts.MOD_ID);

    public static final DeferredItem<IceChargeItem> ICE_CHARGE = ITEMS.registerItem(
            "ice_charge",
            IceChargeItem::new,
            new Item.Properties()
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
