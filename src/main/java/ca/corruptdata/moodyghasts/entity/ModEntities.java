package ca.corruptdata.moodyghasts.entity;

import ca.corruptdata.moodyghasts.entity.projectile.IceChargeEntity;
import ca.corruptdata.moodyghasts.entity.projectile.MoodyIceChargeEntity;
import ca.corruptdata.moodyghasts.entity.projectile.MoodyWindChargeEntity;
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

    public static final DeferredHolder<EntityType<?>, EntityType<IceChargeEntity>> ICE_CHARGE = ENTITIES.register("ice_charge", () -> {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ice_charge")
        );
        return EntityType.Builder
                .<IceChargeEntity>of(IceChargeEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(8)
                .updateInterval(10)
                .build(key);
    });

    public static final DeferredHolder<EntityType<?>, EntityType<MoodyIceChargeEntity>> MOODY_ICE_CHARGE = ENTITIES.register("moody_ice_charge", () -> {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_ice_charge")
        );
        return EntityType.Builder
                .<MoodyIceChargeEntity>of(MoodyIceChargeEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(8)
                .updateInterval(10)
                .build(key);
    });

    public static final DeferredHolder<EntityType<?>, EntityType<MoodyWindChargeEntity>> MOODY_WIND_CHARGE = ENTITIES.register("moody_wind_charge", () -> {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_wind_charge")
        );
        return EntityType.Builder
                .<MoodyWindChargeEntity>of(MoodyWindChargeEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(8)
                .updateInterval(10)
                .build(key);
    });

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}