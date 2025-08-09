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

    public static final Supplier<AttachmentType<Boolean>> IS_CHARGING = ATTACHMENT_TYPES.register(
        "is_charging",
        () -> AttachmentType.builder(() -> false)
            .serialize(Codec.BOOL.fieldOf("is_charging"))
            .sync(ByteBufCodecs.BOOL)
            .build()
    );

    public static final Supplier<AttachmentType<Integer>> CHARGE_TIME = ATTACHMENT_TYPES.register(
        "charge_time",
        () -> AttachmentType.builder(() -> 0)
            .build()
    );


    public static final Supplier<AttachmentType<Item>> PROJECTILE_ITEM = ATTACHMENT_TYPES.register(
        "projectile_item",
        () -> AttachmentType.builder(() -> (Item) null)
            .build()
    );

    public static final Supplier<AttachmentType<Object>> SHOOTING_PLAYER = ATTACHMENT_TYPES.register(
    "shooting_player",
    () -> AttachmentType.builder(() -> null)
        .build()
);
}