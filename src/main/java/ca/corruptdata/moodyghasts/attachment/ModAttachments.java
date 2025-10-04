package ca.corruptdata.moodyghasts.attachment;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MoodyGhasts.MOD_ID);

    public static final Supplier<AttachmentType<Float>> MOOD = ATTACHMENT_TYPES.register(
        "mood",
        () -> AttachmentType.builder(() -> 40.0f) // INITIAL_MOOD
            .serialize(Codec.FLOAT.fieldOf("mood"))
            .sync(ByteBufCodecs.FLOAT)
            .build()
    );

    public static final Supplier<AttachmentType<Boolean>> IS_PREPARING_PROJECTILE = ATTACHMENT_TYPES.register(
        "is_preparing_projectile",
        () -> AttachmentType.builder(() -> false)
            .serialize(Codec.BOOL.fieldOf("is_preparing_projectile"))
            .sync(ByteBufCodecs.BOOL)
            .build()
    );
    public static final Supplier<AttachmentType<Integer>> PROJECTILE_CHARGE_TICK = ATTACHMENT_TYPES.register(
            "projectile_charge_tick",
            () -> AttachmentType.builder(() -> 0)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> IS_SNOWBALL_BARRAGE = ATTACHMENT_TYPES.register(
            "is_snowball_barrage",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL.fieldOf("is_snowball_barrage"))
                    .sync(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Integer>> SNOWBALLS_LEFT = ATTACHMENT_TYPES.register(
            "snowballs_left",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT.fieldOf("snowballs_left"))
                    .sync(ByteBufCodecs.INT)
                    .build()
    );

    public static final Supplier<AttachmentType<Integer>> SNOWBALL_COOLDOWN = ATTACHMENT_TYPES.register(
            "snowball_cooldown",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT.fieldOf("snowball_cooldown"))
                    .sync(ByteBufCodecs.INT)
                    .build()
    );

    public static final Supplier<AttachmentType<Boolean>> IS_CONSUMING_FOOD = ATTACHMENT_TYPES.register(
            "is_consuming_food",
            () -> AttachmentType.builder(() -> false)
                    .serialize(Codec.BOOL.fieldOf("is_consuming_food"))
                    .sync(ByteBufCodecs.BOOL)
                    .build()
    );

    public static final Supplier<AttachmentType<Integer>> FOOD_CONSUME_TICKS = ATTACHMENT_TYPES.register(
            "food_consume_ticks",
            () -> AttachmentType.builder(() -> 0)
                    .build()
    );


    public static final Supplier<AttachmentType<Item>> CURRENT_FOOD = ATTACHMENT_TYPES.register(
            "current_food",
            () -> AttachmentType.builder(() -> (Item) null)
                    .build()
    );

    public static final Supplier<AttachmentType<Item>> CURRENT_PROJECTILE = ATTACHMENT_TYPES.register(
        "current_projectile",
        () -> AttachmentType.builder(() -> (Item) null)
            .build()
    );

    public static final Supplier<AttachmentType<Object>> PROJECTILE_OWNER = ATTACHMENT_TYPES.register(
    "projectile_owner",
    () -> AttachmentType.builder(() -> null)
        .build()
    );
}