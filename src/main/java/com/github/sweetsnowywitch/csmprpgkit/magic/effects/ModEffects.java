package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEffects {
    public static final SpellEffect POTION = new PotionEffect();
    public static final SpellEffect DAMAGE = new DamageEffect();
    public static final SpellEffect MUTE = new MuteEffect();

    public static void register() {
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "potion"), POTION);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "damage"), DAMAGE);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "mute"), MUTE);
    }
}
