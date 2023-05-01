package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellChargeEntity;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class ChargeForm extends SpellForm {
    public ChargeForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var charge = new SpellChargeEntity(ModEntities.SPELL_CHARGE, world);
        cast.customData.putUuid("ChargeEntityUUID", charge.getUuid());
        charge.setCast(cast);
        charge.setPosition(caster.getPos());
        charge.setVelocity(caster, caster.getPitch(), caster.getYaw(), -1.0f, 5f, 1.0f);
        world.spawnEntity(charge);

        super.startCast(cast, world, caster);
    }

    @Override
    public void endCast(ServerSpellCast cast, ServerWorld world) {
        super.endCast(cast, world);

        if (cast.customData.contains("ChargeEntityUUID")) {
            var ent = world.getEntity(cast.customData.getUuid("ChargeEntityUUID"));
            if (ent != null) {
                ent.discard();
            }
        }
    }
}
