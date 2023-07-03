package com.github.sweetsnowywitch.rpgkit.magic.statuseffects;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModStatusEffects {
    public static final StatusEffect MUTE = new MuteStatusEffect();
    public static final StatusEffect SEALED = new SealedStatusEffect();
    public static final ManaRegenStatusEffect MANA_REGEN = new ManaRegenStatusEffect();

    public static void register() {
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMagicMod.MOD_ID, "sound_mute"), MUTE);
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMagicMod.MOD_ID, "sealed"), SEALED);
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMagicMod.MOD_ID, "mana_regen"), MANA_REGEN);
    }
}
