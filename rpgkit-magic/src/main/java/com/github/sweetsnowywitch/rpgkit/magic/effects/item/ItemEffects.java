package com.github.sweetsnowywitch.rpgkit.magic.effects.item;

import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.ItemEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemEffects {
    public static ItemEffect.JsonFactory TRANSMUTE = TransmuteItemEffect::new;

    public static void register() {
        Registry.register(MagicRegistries.ITEM_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "transmute_item"), TRANSMUTE);
    }
}
