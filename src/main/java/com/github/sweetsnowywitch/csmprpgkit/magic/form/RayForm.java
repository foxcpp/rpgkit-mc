package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellRayEntity;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.google.common.collect.ImmutableMap;

public class RayForm extends SpellForm {
    public RayForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(SpellCast cast) {
        var ray = new SpellRayEntity(ModEntities.SPELL_RAY, cast.getWorld());
        ray.setCast(cast);
        ray.setGrowthSpeed(2f);
        ray.setPosition(cast.getCaster().getCameraPosVec(0));
        ray.setYaw(cast.getCaster().getHeadYaw());
        ray.setPitch(cast.getCaster().getPitch());

        cast.getWorld().spawnEntity(ray);

        super.startCast(cast);
    }
}
