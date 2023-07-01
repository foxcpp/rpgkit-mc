package com.github.sweetsnowywitch.rpgkit.magic.form;

import com.github.sweetsnowywitch.rpgkit.magic.entities.ModEntities;
import com.github.sweetsnowywitch.rpgkit.magic.entities.SpellChargeEntity;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellForm;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ChargeForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        private final float bounceVelocityFactor;
        private final float velocity;

        public Reaction(JsonObject obj) {
            super(Type.FORM, obj);

            if (obj.has("bounce_velocity_factor")) {
                this.bounceVelocityFactor = obj.get("bounce_velocity_factor").getAsFloat();
            } else {
                this.bounceVelocityFactor = 0f;
            }

            if (obj.has("velocity")) {
                this.velocity = obj.get("velocity").getAsFloat();
            } else {
                this.velocity = 0f;
            }
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("bounce_velocity_factor", this.bounceVelocityFactor);
            obj.addProperty("velocity", this.velocity);
        }
    }

    public ChargeForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(@NotNull ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var velocity = 1f;
        var bounceFactor = 0.2f;
        for (var reaction : cast.getSpell().getGlobalReactions()) {
            if (reaction instanceof Reaction r) {
                velocity += r.velocity;
                bounceFactor += r.bounceVelocityFactor;
            }
        }
        if (velocity <= 0.5f) {
            velocity = 0.5f;
        }
        bounceFactor = MathHelper.clamp(bounceFactor, 0.1f, 1f);

        var charge = new SpellChargeEntity(ModEntities.SPELL_CHARGE, world);
        cast.customData.putUuid("ChargeEntityUUID", charge.getUuid());
        charge.setCast(cast);
        charge.setPosition(caster.getPos());
        charge.setVelocity(caster, caster.getPitch(), caster.getYaw(), -1.0f, velocity, 1.0f);
        charge.setBounceFactor(bounceFactor);
        world.spawnEntity(charge);

        super.startCast(cast, world, caster);
    }

    @Override
    public void endCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world) {
        super.endCast(cast, world);

        if (cast.customData.contains("ChargeEntityUUID")) {
            var ent = world.getEntity(cast.customData.getUuid("ChargeEntityUUID"));
            if (ent != null) {
                ent.discard();
            }
        }
    }
}
