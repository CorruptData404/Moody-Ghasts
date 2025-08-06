package ca.corruptdata.moodyghasts.item;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.component.ModDataComponentTypes;
import ca.corruptdata.moodyghasts.item.custom.FrostedCookieItem;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import ca.corruptdata.moodyghasts.item.custom.SpicyCookieItem;
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

    public static final DeferredItem<SpicyCookieItem> SPICY_COOKIE = ITEMS.registerItem(
        "spicy_cookie",
        properties -> {
            SpicyCookieItem item = new SpicyCookieItem(properties);
            item.getDefaultInstance().set(ModDataComponentTypes.MOOD_DELTA, 10.0f);
            return item;
        },
        new Item.Properties()
    );

    public static final DeferredItem<FrostedCookieItem> FROSTED_COOKIE = ITEMS.registerItem(
        "frosted_cookie",
        properties -> {
            FrostedCookieItem item = new FrostedCookieItem(properties);
            item.getDefaultInstance().set(ModDataComponentTypes.MOOD_DELTA, -10.0f);
            return item;
        },
        new Item.Properties()
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}