package ca.corruptdata.moodyghasts.entity;

import ca.corruptdata.moodyghasts.entity.projectile.GhastIceChargeEntity;
import ca.corruptdata.moodyghasts.entity.projectile.PlayerIceChargeEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import ca.corruptdata.moodyghasts.MoodyGhasts;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MoodyGhasts.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<PlayerIceChargeEntity>> PLAYER_ICE_CHARGE = ENTITIES.register("ice_charge", () -> {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ice_charge")
        );
        return EntityType.Builder
                .<PlayerIceChargeEntity>of(PlayerIceChargeEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(8)
                .updateInterval(10)
                .build(key);
    });

    public static final DeferredHolder<EntityType<?>, EntityType<GhastIceChargeEntity>> GHAST_ICE_CHARGE = ENTITIES.register("ghast_ice_charge", () -> {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ice_charge")
        );
        return EntityType.Builder
                .<GhastIceChargeEntity>of(GhastIceChargeEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(8)
                .updateInterval(10)
                .build(key);
    });

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
