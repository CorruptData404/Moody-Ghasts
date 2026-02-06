package ca.corruptdata.moodyghasts.datamap;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.Optional;

public class ItemPropertyMap {
    public record Consumable(float moodDelta) {
        public static final Codec<Consumable> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.fieldOf("moodDelta").forGetter(Consumable::moodDelta)
        ).apply(inst, Consumable::new));

        public static final DataMapType<Item, Consumable> DATA_MAP = DataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_consumables_map"),
                Registries.ITEM,
                CODEC
        ).build();
    }
}