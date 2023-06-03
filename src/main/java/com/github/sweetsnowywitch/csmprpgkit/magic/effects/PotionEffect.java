package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PotionEffect extends SpellEffect {
    public static class Reaction extends SpellReaction {
        private final @Nullable StatusEffect effect;
        private final double amplifier;
        private final int durationTicks;

        public Reaction(Identifier id) {
            super(id);
            this.effect = null;
            this.amplifier = 1;
            this.durationTicks = 0;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id, obj);

            if (obj.has("id")) {
                var effectId = new Identifier(obj.get("id").getAsString());
                var effect = Registry.STATUS_EFFECT.get(effectId);
                RPGKitMod.LOGGER.debug("PotionEffect.Reaction populated with potion effect {}", effectId);
                if (effect == null) {
                    throw new IllegalStateException("unknown potion effect");
                }
                this.effect = effect;
            } else {
                this.effect = null;
            }

            if (obj.has("amplifier")) {
                this.amplifier = obj.get("amplifier").getAsDouble();
            } else {
                this.amplifier = 1;
            }
            if (obj.has("duration")) {
                this.durationTicks = obj.get("duration").getAsInt();
            } else {
                this.durationTicks = 0;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof PotionEffect;
        }

        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);

            if (this.effect != null) {
                var id = Registry.STATUS_EFFECT.getId(this.effect);
                if (id == null) {
                    throw new IllegalStateException("potion effect with unregistered effect");
                }
                obj.addProperty("id", id.toString());
            }

            obj.addProperty("amplifier", this.amplifier);
            obj.addProperty("duration", this.durationTicks);
        }

        @Override
        public String toString() {
            return "PotionEffect.Reaction[" +
                    "amplifier=" + amplifier +
                    ", durationTicks=" + durationTicks +
                    ']';
        }
    }

    public static final int DEFAULT_DURATION = 20 * 10;
    public static final int DEFAULT_AMPLIFIER = 1;

    private final StatusEffect statusEffect;
    private final int baseDuration;
    private final int baseAmplifier;

    public PotionEffect(Identifier id) {
        super(id);
        this.statusEffect = null;
        this.baseAmplifier = DEFAULT_AMPLIFIER;
        this.baseDuration = DEFAULT_DURATION;
    }

    public PotionEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("id")) {
            var effectId = new Identifier(obj.get("id").getAsString());
            var effect = Registry.STATUS_EFFECT.get(effectId);
            RPGKitMod.LOGGER.debug("PotionEffect populated with potion effect {}", effectId);
            if (effect == null) {
                throw new IllegalStateException("unknown potion effect");
            }
            this.statusEffect = effect;
        } else {
            this.statusEffect = null;
        }
        if (obj.has("amplifier")) {
            this.baseAmplifier = obj.get("amplifier").getAsInt();
        } else {
            this.baseAmplifier = DEFAULT_AMPLIFIER;
        }

        if (obj.has("duration")) {
            this.baseDuration = obj.get("duration").getAsInt();
        } else {
            this.baseDuration = DEFAULT_DURATION;
        }
    }

    public String toString() {
        if (this.statusEffect == null) {
            return "PotionEffect[]";
        }
        return "PotionEffect[%s,amp=%d,dur=%s]".formatted(
                Objects.requireNonNull(Registry.STATUS_EFFECT.getId(this.statusEffect)).toString(),
                this.baseAmplifier, this.baseDuration);
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        if (this.statusEffect == null) {
            RPGKitMod.LOGGER.warn("Cast {} with empty status effect", cast);
            return false;
        }
        if (!(entity instanceof LivingEntity le)) {
            return false;
        }

        double amplifier = this.baseAmplifier;
        var duration = this.baseDuration;

        for (var reaction : cast.getReactions()) {
            if (reaction instanceof Reaction r && (r.effect == null || r.effect.equals(this.statusEffect))) {
                amplifier += r.amplifier;
                duration += r.durationTicks;
            }
        }
        if (amplifier <= 0) {
            amplifier = 0;
        }
        if (duration <= 2) {
            duration = 2;
        }

        var caster = ((ServerWorld) entity.getWorld()).getEntity(cast.getCasterUuid());

        le.addStatusEffect(
                new StatusEffectInstance(this.statusEffect, duration, (int) amplifier, false, false),
                caster
        );

        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        return false;
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {

    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        if (this.statusEffect != null) {
            var id = Registry.STATUS_EFFECT.getId(this.statusEffect);
            if (id == null) {
                throw new IllegalStateException("potion effect with unregistered effect");
            }
            obj.addProperty("id", id.toString());
        }
        obj.addProperty("amplifier", this.baseAmplifier);
        obj.addProperty("duration", this.baseDuration);
    }
}
