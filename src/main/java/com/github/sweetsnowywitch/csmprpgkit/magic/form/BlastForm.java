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
            super(id);
            this.radius = 0;
            this.distance = 0;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id);
            if (obj.has("radius")) {
                this.radius = obj.get("radius").getAsDouble();
            } else {
                this.radius = 0;
            }
            if (obj.has("distance")) {
                this.distance = obj.get("distance").getAsDouble();
            } else {
                this.distance = 0;
            }
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("radius", this.radius);
            obj.addProperty("distance", this.distance);
        }
    }

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

        double distance = 5;
        double radius = 1.25;
        for (var reaction : cast.getReactions()) {
            if (reaction instanceof Reaction r) {
                distance += r.distance;
                radius += r.radius;
            }
        }

        var blast = new SpellBlastEntity(ModEntities.SPELL_BLAST, world, pos,
                distance, radius, caster.getRotationVector(), 15);
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
