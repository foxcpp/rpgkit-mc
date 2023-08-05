package com.github.foxcpp.rpgkitmc.magic.effects.item;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemEffects {
    public static void register() {
        Registry.register(MagicRegistries.ITEM_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "transmute_item"), TransmuteItemEffect::new);
        Registry.register(MagicRegistries.ITEM_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "give_item"), GiveItemEffect::new);
        Registry.register(MagicRegistries.ITEM_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "cook_item"), CookItemEffect::new);
        Registry.register(MagicRegistries.ITEM_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "caster_apply"), CasterApplyEffect::new);
        Registry.register(MagicRegistries.ITEM_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "consume_item"), ConsumeItemEffect::new);
    }
}
