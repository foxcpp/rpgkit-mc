package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {
    public static SpellEffect POTION = new PotionEffect();

    public static void register() {
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "potion"), POTION);
    }
}
