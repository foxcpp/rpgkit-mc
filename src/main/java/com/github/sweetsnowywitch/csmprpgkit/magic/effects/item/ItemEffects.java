package com.github.sweetsnowywitch.csmprpgkit.magic.effects.item;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.ItemEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemEffects {
    public static ItemEffect.JsonFactory TRANSMUTE = TransmuteItemEffect::new;

    public static void register() {
        Registry.register(MagicRegistries.ITEM_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "transmute_item"), TRANSMUTE);
    }
}
