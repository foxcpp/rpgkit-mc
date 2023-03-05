package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.google.common.collect.ImmutableList;

public class Spell {
    private final ImmutableList<SpellEffect> effects;

    public Spell(ImmutableList<SpellEffect> effects) {
        this.effects = effects;
    }

    public ImmutableList<SpellEffect> getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        var id = ModRegistries.SPELLS.inverse().get(this);
        if (id == null) {
            throw new IllegalStateException("toString called for unregistered spell");
        }
        return id.toString();
    }
}
