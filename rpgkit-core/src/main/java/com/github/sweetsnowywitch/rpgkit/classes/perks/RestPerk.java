package com.github.sweetsnowywitch.rpgkit.classes.perks;

import com.github.sweetsnowywitch.rpgkit.RPGKitMod;
import com.github.sweetsnowywitch.rpgkit.classes.Perk;
import com.github.sweetsnowywitch.rpgkit.classes.ServerTickablePerk;
import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class RestPerk extends Perk implements ServerTickablePerk {
    public static final int DEFAULT_DURATION = 20 * 10;
    public static final int DEFAULT_AMPLIFIER = 0;
    public static final int DEFAULT_REST_TIME = 20 * 10;

    private final StatusEffect statusEffect;
    private final int duration;
    private final int amplifier;
    private final int restTime;
    private int timer;

    public RestPerk(Identifier typeId) {
        super(typeId);
        this.statusEffect = null;
        this.duration = DEFAULT_DURATION;
        this.amplifier = DEFAULT_AMPLIFIER;
        this.restTime = DEFAULT_REST_TIME;
        this.timer = 0;
    }

    public RestPerk(Identifier typeId, @Nullable StatusEffect statusEffect, int duration, int amplifier, int restTime) {
        super(typeId);
        this.statusEffect = statusEffect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.restTime = restTime;
        this.timer = 0;
    }

    @Override
    public Perk withParametersFromJSON(JsonObject obj) {
        var effect = this.statusEffect;
        if (obj.has("id")) {
            var id = new Identifier(obj.get("id").getAsString());
            effect = Registry.STATUS_EFFECT.get(id);
            RPGKitMod.LOGGER.debug("StatusEffectPerk populated with potion effect {}", id);
            if (effect == null) {
                throw new IllegalStateException("unknown potion effect");
            }
        }

        var duration = this.duration;
        if (obj.has("duration")) {
            duration = obj.get("duration").getAsInt();
        }

        var amplifier = this.amplifier;
        if (obj.has("amplifier")) {
            amplifier = obj.get("amplifier").getAsInt();
        }

        var restTime = this.restTime;
        if (obj.has("restTime")) {
            restTime = obj.get("restTime").getAsInt();
        }
        return new RestPerk(this.typeId, effect, duration, amplifier, restTime);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        if (this.statusEffect != null) {
            var id = Registry.STATUS_EFFECT.getId(this.statusEffect);
            if (id == null) {
                throw new IllegalStateException("rest perk with unregistered effect");
            }
            obj.addProperty("id", id.toString());
        }
        obj.addProperty("duration", this.duration);
        obj.addProperty("amplifier", this.amplifier);
        obj.addProperty("restTime", this.restTime);
        return obj;
    }

    @Override
    public void tick(ServerPlayerEntity entity) {

        if (entity.getAttacking() == null && entity.getAttacker() == null) {
            this.timer++;
            if (this.timer >= this.restTime) {
                if (this.statusEffect == null) {
                    RPGKitMod.LOGGER.warn("RestPerk on {} with empty status effect", entity);
                    return;
                }

                entity.addStatusEffect(new StatusEffectInstance(this.statusEffect, this.duration, this.amplifier,
                        false, false), entity);
            }
        } else {
            this.timer = 0;
        }
    }
}