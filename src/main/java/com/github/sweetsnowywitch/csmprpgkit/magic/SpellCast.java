package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpellCast {
    private final SpellForm form;
    private final Spell spell;
    private final LivingEntity caster;
    private final ImmutableList<SpellReaction> formReactions;
    private final ImmutableList<SpellReaction> effectReactions;

    public SpellCast(SpellForm form, Spell spell, @NotNull LivingEntity caster, List<SpellReaction> formReactions,
                     List<SpellReaction> effectReactions) {
        this.form = form;
        this.spell = spell;
        this.caster = caster;
        this.formReactions = ImmutableList.copyOf(formReactions);
        this.effectReactions = ImmutableList.copyOf(effectReactions);
    }

    public void perform() {
        if (this.caster.getWorld().isClient) {
            throw new IllegalStateException("cannot perform SpellCast on the client");
        }

        for (var effect : this.spell.getEffects()) {
            effect.onCast(this, this.getEffectReactions());
            RPGKitMod.LOGGER.debug("Applying effect {}", effect);
            this.form.apply(this, effect);
        }
    }

    public SpellForm getForm() {
        return form;
    }

    public Spell getSpell() {
        return spell;
    }

    public LivingEntity getCaster() {
        return this.caster;
    }

    public ImmutableList<SpellReaction> getFormReactions() {
        return formReactions;
    }

    public ImmutableList<SpellReaction> getEffectReactions() {
        return effectReactions;
    }

    @Override
    public String toString() {
        return "SpellSpell[form=%s,spell=%s,caster=%s]".formatted(this.form.toString(), this.spell.toString(), this.caster.toString());
    }
}
