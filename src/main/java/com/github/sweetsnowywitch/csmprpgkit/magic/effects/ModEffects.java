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
    public static final SpellEffect PUSH_AWAY = new PushAwayEffect();
    public static final SpellEffect FIRE = new FireEffect();
    public static final SpellEffect EXTINGUISH = new ExtinguishEffect();

    public static void register() {
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "potion"), POTION);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "damage"), DAMAGE);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "mute"), MUTE);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "push_away"), PUSH_AWAY);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "fire"), FIRE);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "extinguish"), EXTINGUISH);
    }
}
