package com.github.foxcpp.rpgkitmc.magic.statuseffects;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class KnockbackResistanceEffect extends StatusEffect {
    protected KnockbackResistanceEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFFFFF);
    }
}
