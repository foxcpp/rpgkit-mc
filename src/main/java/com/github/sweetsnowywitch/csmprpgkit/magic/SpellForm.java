package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class SpellForm {
    private final ImmutableMap<String, Float> costMultipliers;
    private final ImmutableMap<String, Float> costTerms;

    public @Nullable SpellReaction reactionType() {
        return null;
    }

    public SpellForm(ImmutableMap<String, Float> costMultipliers, ImmutableMap<String, Float> costTerms) {
        this.costMultipliers = costMultipliers;
        this.costTerms = costTerms;
    }

    public float getCostMultiplier(String key) {
        return Objects.requireNonNull(costMultipliers.getOrDefault(key, (float) 1));
    }

    public float getCostTerm(String key) {
        return Objects.requireNonNull(costTerms.getOrDefault(key, (float) 0));
    }

    public float applyCost(String key, float val) {
        return val * this.getCostMultiplier(key) + this.getCostTerm(key);
    }

    public abstract void apply(SpellCast cast, SpellEffect effect);

    public String toString() {
        var id = ModRegistries.SPELL_FORMS.getId(this);
        if (id == null) {
            throw new IllegalStateException("toString called to unregistered SpellForm");
        }
        return id.toString();
    }
}
