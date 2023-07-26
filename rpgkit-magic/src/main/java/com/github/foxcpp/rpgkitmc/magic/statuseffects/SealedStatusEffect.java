package com.github.foxcpp.rpgkitmc.magic.statuseffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class SealedStatusEffect extends StatusEffect {
    public interface Sealable {
        void setSealed(boolean v);
    }

    public SealedStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFFFFF);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        ((Sealable) entity).setSealed(true);
        entity.setAir(entity.getAir() - 1);
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        ((Sealable) entity).setSealed(true);
        // Air is updated in LivingEntityMixin.
        if (entity.getAir() == 0) {
            entity.damage(DamageSource.IN_WALL, 2.0f);
        }
        super.applyUpdateEffect(entity, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        ((Sealable) entity).setSealed(false);
        super.onRemoved(entity, attributes, amplifier);
    }
}
