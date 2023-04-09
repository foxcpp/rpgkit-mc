package com.github.sweetsnowywitch.csmprpgkit.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModStatusEffects {
    public static final StatusEffect MUTE = new MuteStatusEffect();

    public static void register() {
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMod.MOD_ID, "sound_mute"), MUTE);
    }
}
