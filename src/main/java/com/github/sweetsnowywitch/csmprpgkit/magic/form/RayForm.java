package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellRayEntity;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RayForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        private final int addBounces;

        public Reaction(Identifier id) {
            this(id, 0);
        }

        public Reaction(Identifier id, int addBounces) {
            super(id);
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
            return new Reaction(this.id, addBounces);
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
    public @Nullable SpellReaction reactionType(Identifier id) {
        return new Reaction(id);
    }

    @Override
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var ray = new SpellRayEntity(ModEntities.SPELL_RAY, world);
        ray.setCast(cast);
        ray.setGrowthSpeed(2f);

        var pos = caster.getPos();
        if (caster instanceof PlayerEntity pe) {
            pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
            pos = pos.add(0, 0.7, 0);
        }
        ray.setPosition(pos);
        ray.setYaw(caster.getHeadYaw());
        ray.setPitch(caster.getPitch());

        var bounces = 0;
        for (var reaction : cast.getFormReactions()) {
            if (reaction instanceof Reaction r) {
                bounces += r.addBounces;
            }
        }
        ray.setTotalBounces(bounces);

        world.spawnEntity(ray);

        super.startCast(cast, world, caster);
    }
}
