package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.google.common.collect.ImmutableMap;

/**
 * Placeholder for form used when spell is directly applied to a block or entity,
 * actual interaction happens in ActiveCastComponent.
 */
public class UseForm extends SpellForm {
    public UseForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }
}
