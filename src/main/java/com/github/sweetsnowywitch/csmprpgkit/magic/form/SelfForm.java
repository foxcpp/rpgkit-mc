package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.google.common.collect.ImmutableMap;

public class SelfForm extends SpellForm {
    public SelfForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(SpellCast cast) {
        for (var effect : cast.getSpell().getEffects()) {
            effect.onSelfHit(cast, cast.getEffectReactions());
        }
    }

    @Override
    public void endCast(SpellCast cast) {
        for (var effect : cast.getSpell().getEffects()) {
            effect.endCast(cast, cast.getEffectReactions());
        }
    }
}
