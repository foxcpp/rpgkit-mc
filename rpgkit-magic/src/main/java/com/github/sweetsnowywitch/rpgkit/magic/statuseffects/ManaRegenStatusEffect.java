package com.github.sweetsnowywitch.rpgkit.magic.statuseffects;

import com.github.sweetsnowywitch.rpgkit.magic.components.ModComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ManaRegenStatusEffect extends StatusEffect {
    public ManaRegenStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0xFFFFFF);
    }

    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (this == ModStatusEffects.MANA_REGEN) {
            var mana = entity.getComponent(ModComponents.MANA);
            mana.regenerate(mana.getRegen(), (amplifier + 1) * 0.1, mana.getMaxValue());
        }
    }

    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        if (this == ModStatusEffects.MANA_REGEN) {
            var i = 50 >> amplifier;
            if (i > 0) {
                return duration % i == 0;
            }
            return true;
        }
        return false;
    }
}
