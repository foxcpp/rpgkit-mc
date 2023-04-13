package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;

public class AreaForm extends SpellForm {
    public AreaForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        super.startCast(cast, world, caster);

        var area = Box.of(caster.getPos(), 10, 10, 10);
        cast.getSpell().onAreaHit(cast, world, area);
    }

    @Override
    public void endCast(ServerSpellCast cast, ServerWorld world) {
        super.endCast(cast, world);
    }
}
