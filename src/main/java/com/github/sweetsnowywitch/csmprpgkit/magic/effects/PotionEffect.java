package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PotionEffect extends SpellEffect {
    public static class Reaction extends SpellReaction {
        private final int amplifier;
        private final int durationTicks;

        protected Reaction() {
            this(0, 0);
        }

        protected Reaction(int amplifier, int durationTicks) {
            this.amplifier = amplifier;
            this.durationTicks = durationTicks;
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof PotionEffect;
        }

        @Override
        public SpellReaction withParametersFromJSON(JsonObject jsonObject) {
            var amplifier = this.amplifier;
            if (jsonObject.has("amplifier")) {
                amplifier = jsonObject.get("amplifier").getAsInt();
            }
            var durationTicks = this.durationTicks;
            if (jsonObject.has("duration")) {
                durationTicks = jsonObject.get("duration").getAsInt();
            }
            var r = new Reaction(amplifier, durationTicks);
            r.populateFromJson(jsonObject);
            return r;
        }

        @Override
        public JsonObject parametersToJSON() {
            var obj = new JsonObject();
            obj.addProperty("amplifier", this.amplifier);
            obj.addProperty("duration", this.durationTicks);
            return obj;
        }

        @Override
        public String toString() {
            return "Reaction[" +
                    "amplifier=" + amplifier +
                    ", durationTicks=" + durationTicks +
                    ']';
        }
    }

    public static final int DEFAULT_DURATION = 20*10;
    public static final int DEFAULT_AMPLIFIER = 1;

    private final StatusEffect statusEffect;
    private final int baseDuration;
    private final int baseAmplifier;

    public PotionEffect() {
        this.statusEffect = null;
        this.baseAmplifier = DEFAULT_AMPLIFIER;
        this.baseDuration = DEFAULT_DURATION;
    }

    public PotionEffect(@Nullable StatusEffect statusEffect, int baseAmplifier, int baseDuration) {
        this.statusEffect = statusEffect;
        this.baseAmplifier = baseAmplifier;
        this.baseDuration = baseDuration;
    }

    public String toString() {
        if (this.statusEffect == null) {
            return "PotionEffect[]";
        }
        return "PotionEffect[%s,amp=%d,dur=%s]".formatted(
                Objects.requireNonNull(Registries.STATUS_EFFECT.getId(this.statusEffect)).toString(),
                this.baseAmplifier, this.baseDuration);
    }

    @Override
    public @Nullable SpellReaction reactionType() {
        return new Reaction();
    }

    @Override
    public void onSingleEntityHit(SpellCast cast, Entity entity, ImmutableList<SpellReaction> reactions) {
        if (this.statusEffect == null) {
            RPGKitMod.LOGGER.warn("Cast {} with empty status effect", cast);
            return;
        }
        if (!(entity instanceof LivingEntity le)) {
            return;
        }

        var amplifier = this.baseAmplifier;
        var duration = this.baseDuration;

        for (var reaction : reactions) {
            if (reaction.appliesTo(this)) {
                var peReaction = (Reaction)reaction;
                amplifier += peReaction.amplifier;
                duration += peReaction.durationTicks;
            }
        }

        le.addStatusEffect(
                new StatusEffectInstance(this.statusEffect, duration, amplifier, false, false),
                cast.getCaster()
        );
    }

    @Override
    public void onSingleBlockHit(SpellCast cast, BlockPos pos, Direction dir, ImmutableList<SpellReaction> reactions) {

    }

    @Override
    public void onAreaHit(SpellCast cast, Vec3d position, ImmutableList<SpellReaction> reactions) {

    }

    @Override
    public SpellEffect withParametersFromJSON(JsonObject obj) {
        StatusEffect effect = this.statusEffect;
        if (obj.has("id")) {
            var id = new Identifier(obj.get("id").getAsString());
            effect = Registries.STATUS_EFFECT.get(id);
            RPGKitMod.LOGGER.debug("PotionEffect populated with potion effect {}", id);
            if (effect == null) {
                throw new IllegalStateException("unknown potion effect");
            }
        }
        int baseAmplifier = this.baseAmplifier;
        if (obj.has("amplifier")) {
            baseAmplifier = obj.get("amplifier").getAsInt();
        }
        int baseDuration = this.baseDuration;
        if (obj.has("duration")) {
            baseDuration = obj.get("duration").getAsInt();
        }
        return new PotionEffect(effect, baseAmplifier, baseDuration);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        if (this.statusEffect != null) {
            var id = Registries.STATUS_EFFECT.getId(this.statusEffect);
            if (id == null) {
                throw new IllegalStateException("potion effect with unregistered effect");
            }
            obj.addProperty("id", id.toString());
        }
        obj.addProperty("amplifier", this.baseAmplifier);
        obj.addProperty("duration", this.baseDuration);
        return obj;
    }
}
