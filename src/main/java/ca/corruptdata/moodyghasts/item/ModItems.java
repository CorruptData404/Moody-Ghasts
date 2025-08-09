package ca.corruptdata.moodyghasts.item;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.component.ModDataComponentTypes;
import ca.corruptdata.moodyghasts.item.custom.FrostedCookieItem;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import ca.corruptdata.moodyghasts.item.custom.SpicyCookieItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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

    public static final DeferredItem<FrostedCookieItem> FROSTED_COOKIE = ITEMS.register("frosted_cookie",
            registryName -> new FrostedCookieItem(
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, registryName))
                            .component(ModDataComponentTypes.MOOD_DELTA.get(), -10.0f)
            )
    );

    public static final DeferredItem<SpicyCookieItem> SPICY_COOKIE = ITEMS.register("spicy_cookie",
            registryName -> new SpicyCookieItem(
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, registryName))
                            .component(ModDataComponentTypes.MOOD_DELTA.get(), 10.0f)
            )
    );


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}