package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellRayEntity;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

public class RayForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        private final int addBounces;

        public Reaction() {
            this(0);
        }

        public Reaction(int addBounces) {
            this.addBounces = addBounces;
        }

        @Override
        public boolean appliesTo(SpellForm form) {
            return form instanceof RayForm;
        }

        @Override
        public SpellReaction withParametersFromJSON(JsonObject jsonObject) {
            var addBounces = this.addBounces;
            if (jsonObject.has("bounces")) {
                addBounces = jsonObject.get("bounces").getAsInt();
            }
            return new Reaction(addBounces);
        }

        @Override
        public JsonObject parametersToJSON() {
            var res = new JsonObject();
            res.addProperty("bounces", this.addBounces);
            return null;
        }
    }

    public RayForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public @Nullable SpellReaction reactionType() {
        return new Reaction();
    }

    @Override
    public void startCast(SpellCast cast) {
        var ray = new SpellRayEntity(ModEntities.SPELL_RAY, cast.getWorld());
        ray.setCast(cast);
        ray.setGrowthSpeed(2f);
        ray.setPosition(cast.getCaster().getCameraPosVec(0));
        ray.setYaw(cast.getCaster().getHeadYaw());
        ray.setPitch(cast.getCaster().getPitch());

        var bounces = 0;
        for (var reaction : cast.getFormReactions()) {
            if (reaction instanceof Reaction r) {
                bounces += r.addBounces;
            }
        }
        ray.setRemainingBounces(bounces);

        cast.getWorld().spawnEntity(ray);

        super.startCast(cast);
    }
}
