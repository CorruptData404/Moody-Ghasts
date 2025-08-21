package ca.corruptdata.moodyghasts.entity;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = MoodyGhasts.MOD_ID)
public class SkeletonConversionHandler {

    private static final ResourceKey<DamageType> ICECHARGE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ice_charge"));

    @SubscribeEvent
    public static void onIceChargeHit(LivingDamageEvent.Post event) {
        if (!event.getSource().is(ICECHARGE_DAMAGE)) return;
        if (!(event.getEntity() instanceof Skeleton skeleton)) return;
        if (!skeleton.isAlive() || skeleton.isNoAi()) return;

        // Instant conversion to Stray
        skeleton.convertTo(
                EntityType.STRAY,
                new ConversionParams(ConversionType.SINGLE, true, true, null),
                newEntity -> {
                    net.neoforged.neoforge.event.EventHooks.onLivingConvert(skeleton, newEntity);

                    if (!skeleton.isSilent()) {
                        skeleton.level().levelEvent(null, 1048, skeleton.blockPosition(), 0);
                    }
                }
        );
    }
}