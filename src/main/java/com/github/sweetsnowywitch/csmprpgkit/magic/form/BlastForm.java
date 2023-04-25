package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellBlastEntity;
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

public class BlastForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        public final double radius;
        public final double distance;

        public Reaction(Identifier id) {
            this(id,0,0);
        }

        public Reaction(Identifier id, double radius, double distance) {
            super(id);
            this.radius = radius;
            this.distance = distance;
        }

        @Override
        public SpellReaction withParametersFromJSON(JsonObject jsonObject) {
            var radius = this.radius;
            if (jsonObject.has("radius")) {
                radius = jsonObject.get("radius").getAsDouble();
            }
            var distance = this.distance;
            if (jsonObject.has("distance")) {
                distance = jsonObject.get("distance").getAsDouble();
            }
            return new Reaction(this.id, radius, distance);
        }

        @Override
        public JsonObject parametersToJSON() {
            var obj = new JsonObject();
            obj.addProperty("radius", this.radius);
            obj.addProperty("distance", this.distance);
            return obj;
        }
    }

    public BlastForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public @Nullable SpellReaction reactionType(Identifier id) {
        return new Reaction(id);
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
