package ca.corruptdata.moodyghasts.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import ca.corruptdata.moodyghasts.MoodyGhasts;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> HAPPY_GHAST_PROJECTILES = createTag("happy_ghast_projectiles");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, name));
        }
    }
}