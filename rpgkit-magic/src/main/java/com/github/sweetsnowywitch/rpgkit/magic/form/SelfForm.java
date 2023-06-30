package com.github.sweetsnowywitch.rpgkit.magic.form;

import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellForm;
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

        cast.getSpell().useOnEntity(cast, caster);
    }
}
