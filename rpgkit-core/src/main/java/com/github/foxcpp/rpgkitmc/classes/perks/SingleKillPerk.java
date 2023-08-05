package com.github.foxcpp.rpgkitmc.classes.perks;

import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.classes.Perk;
import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SingleKillPerk extends Perk {
    public static final int DEFAULT_DURATION = 20 * 10;
    public static final int DEFAULT_AMPLIFIER = 0;

    private final StatusEffect statusEffect;
    private final boolean onlyPlayerKill;
    private final int duration;
    private final int amplifier;

    public SingleKillPerk(Identifier typeId) {
        super(typeId);
        this.statusEffect = null;
        this.onlyPlayerKill = false;
        this.duration = DEFAULT_DURATION;
        this.amplifier = DEFAULT_AMPLIFIER;
    }

    public SingleKillPerk(Identifier typeId, @Nullable StatusEffect statusEffect, boolean onlyPlayerKill, int duration, int amplifier) {
        super(typeId);
        this.statusEffect = statusEffect;
        this.onlyPlayerKill = onlyPlayerKill;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public Perk withParametersFromJSON(JsonObject obj) {
        var effect = this.statusEffect;
        if (obj.has("id")) {
            var id = new Identifier(obj.get("id").getAsString());
            effect = Registries.STATUS_EFFECT.get(id);
            RPGKitMod.LOGGER.debug("StatusEffectPerk populated with potion effect {}", id);
            if (effect == null) {
                throw new IllegalStateException("unknown potion effect");
            }
        }

        var playerKill = this.onlyPlayerKill;
        if (obj.has("playerKill")) {
            playerKill = obj.get("playerKill").getAsBoolean();
        }

        var amplifier = this.amplifier;
        if (obj.has("amplifier")) {
            amplifier = obj.get("amplifier").getAsInt();
        }

        var duration = this.duration;
        if (obj.has("duration")) {
            duration = obj.get("duration").getAsInt();
        }
        return new SingleKillPerk(this.typeId, effect, playerKill, duration, amplifier);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        if (this.statusEffect != null) {
            var id = Registries.STATUS_EFFECT.getId(this.statusEffect);
            if (id == null) {
                throw new IllegalStateException("on kill perk with unregistered effect");
            }
            obj.addProperty("id", id.toString());
        }
        obj.addProperty("playerKill", this.onlyPlayerKill);
        obj.addProperty("duration", this.duration);
        obj.addProperty("amplifier", this.amplifier);
        return obj;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    public boolean isOnlyPlayerKill() {
        return onlyPlayerKill;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }
}
