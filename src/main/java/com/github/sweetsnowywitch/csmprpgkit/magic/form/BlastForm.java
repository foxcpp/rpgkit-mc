package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellBlastEntity;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class BlastForm extends SpellForm {
    public BlastForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var pos = caster.getPos();
        if (caster instanceof PlayerEntity pe) {
            pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
            pos = pos.add(0, 0.7, 0);
        }

        var blast = new SpellBlastEntity(ModEntities.SPELL_BLAST, world, pos,
                5, 1.25, caster.getRotationVector(), 15);
        blast.setCast(cast);
        world.spawnEntity(blast);

        super.startCast(cast, world, caster);

        cast.getSpell().onAreaHit(cast, world, blast.getArea());
    }

    @Override
    public void endCast(ServerSpellCast cast, ServerWorld world) {
        super.endCast(cast, world);
    }
}
