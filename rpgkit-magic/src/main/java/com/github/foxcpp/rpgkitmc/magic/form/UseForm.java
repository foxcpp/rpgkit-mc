package com.github.foxcpp.rpgkitmc.magic.form;

import com.github.foxcpp.rpgkitmc.magic.spell.SpellForm;
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
