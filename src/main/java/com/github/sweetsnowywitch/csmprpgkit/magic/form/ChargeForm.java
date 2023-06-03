package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellChargeEntity;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class ChargeForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        private final float bounceVelocityFactor;
        private final float velocity;

        public Reaction(Identifier id) {
            super(id);
            this.bounceVelocityFactor = 0f;
            this.velocity = 0f;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id, obj);

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
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var velocity = 1f;
        var bounceFactor = 0.2f;
        for (var reaction : cast.getReactions()) {
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
