package com.github.foxcpp.rpgkitmc.magic.form;

import com.github.foxcpp.rpgkitmc.magic.entities.ModEntities;
import com.github.foxcpp.rpgkitmc.magic.entities.SpellBlastEntity;
import com.github.foxcpp.rpgkitmc.magic.items.ModItems;
import com.github.foxcpp.rpgkitmc.magic.spell.*;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

public class BlastForm extends SpellForm implements ChanneledForm {
    public static class Reaction extends SpellReaction {
        public final double radius;
        public final double distance;

        public Reaction(JsonObject obj) {
            super(Type.FORM, obj);

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

        @Override
        public String toString() {
            return "BlastForm.Reaction[" +
                    "radius=" + radius +
                    ", distance=" + distance +
                    ']';
        }
    }

    public BlastForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(@NotNull ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var pos = caster.getPos();
        if (caster instanceof PlayerEntity pe) {
            pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
            pos = pos.add(0, 0.7, 0);
        }

        double distance = 5;
        double radius = 1.25;
        for (var reaction : cast.getSpell().getGlobalReactions()) {
            if (reaction instanceof Reaction r) {
                distance += r.distance;
                radius += r.radius;
            }
        }
        if (distance <= 3) {
            distance = 3;
        }
        if (radius <= 0) {
            radius = 1.25;
        }

        var blast = new SpellBlastEntity(ModEntities.SPELL_BLAST, world, pos,
                distance, radius, caster.getRotationVector(), 5);
        cast.customData.putUuid("BlastEntityUUID", blast.getUuid());
        cast.customData.putDouble("BlastDistance", distance);
        cast.customData.putDouble("BlastRadius", radius);
        blast.setCast(cast);
        world.spawnEntity(blast);

        super.startCast(cast, world, caster);
    }

    @Override
    public void endCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world) {
        super.endCast(cast, world);

        if (cast.customData.contains("BlastEntityUUID")) {
            var ent = world.getEntity(cast.customData.getUuid("BlastEntityUUID"));
            if (ent != null) {
                ent.discard();
            }
        }
    }

    @Override
    public int getMaxChannelDuration(Spell cast) {
        return 4 * 20;
    }

    @Override
    public void channelTick(ServerSpellCast cast, @NotNull Entity caster) {
        if (cast.customData.containsUuid("BlastEntityUUID")) {
            var blast = (SpellBlastEntity) ((ServerWorld) caster.getWorld()).getEntity(cast.customData.getUuid("BlastEntityUUID"));
            if (blast != null) {
                var pos = caster.getPos();
                if (caster instanceof PlayerEntity pe) {
                    pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
                    pos = pos.add(0, 0.7, 0);
                }

                double distance = cast.customData.getDouble("BlastDistance");
                double radius = cast.customData.getDouble("BlastRadius");

                cast.updateOrigin(pos);
                cast.updateOriginRotation(caster.getHeadYaw(), caster.getPitch());
                blast.moveArea(pos, caster.getRotationVector(), distance, radius);
                blast.increaseMaxAge(5);
            }
        }
    }
}
