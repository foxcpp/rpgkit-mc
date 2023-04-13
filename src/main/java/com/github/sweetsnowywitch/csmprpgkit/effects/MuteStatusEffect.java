package com.github.sweetsnowywitch.csmprpgkit.effects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class MuteStatusEffect extends StatusEffect {
    public static final int AMPLIFIER_MUTE_INSIDE = 1;
    public static final int AMPLIFIER_CRUDE = 0;

    public MuteStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xFFFFFF);
    }
}

