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
    public void apply(SpellCast cast, SpellEffect effect) {
        effect.onSelfHit(cast, cast.getEffectReactions());
    }
}
