package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class SelfForm extends SpellForm {
    public SelfForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world, @NotNull Entity caster) {
        super.startCast(cast, world, caster);
        for (var effect : cast.getSpell().getEffects()) {
            effect.onSingleEntityHit(cast, caster);
        }
    }
}
