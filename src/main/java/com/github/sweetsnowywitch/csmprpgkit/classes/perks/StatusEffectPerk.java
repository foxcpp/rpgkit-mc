package com.github.sweetsnowywitch.csmprpgkit.classes.perks;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.github.sweetsnowywitch.csmprpgkit.classes.ServerTickablePerk;
import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class StatusEffectPerk extends Perk implements ServerTickablePerk {
    public static final int DEFAULT_DURATION = 20*10;
    public static final int DEFAULT_AMPLIFIER = 1;

    private final StatusEffect statusEffect;
    private final int baseDuration;
    private final int baseAmplifier;

    public StatusEffectPerk(Identifier typeId) {
        super(typeId);
        this.statusEffect = null;
        this.baseAmplifier = DEFAULT_AMPLIFIER;
        this.baseDuration = DEFAULT_DURATION;
    }

    public StatusEffectPerk(Identifier typeId, @Nullable StatusEffect statusEffect, int baseAmplifier, int baseDuration) {
        super(typeId);
        this.statusEffect = statusEffect;
        this.baseAmplifier = baseAmplifier;
        this.baseDuration = baseDuration;
    }

    public StatusEffectPerk withParametersFromJSON(JsonObject obj) {
        StatusEffect effect = this.statusEffect;
        if (obj.has("id")) {
            var id = new Identifier(obj.get("id").getAsString());
            effect = Registry.STATUS_EFFECT.get(id);
            RPGKitMod.LOGGER.debug("StatusEffectPerk populated with potion effect {}", id);
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
        return new StatusEffectPerk(this.typeId, effect, baseAmplifier, baseDuration);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        if (this.statusEffect != null) {
            var id = Registry.STATUS_EFFECT.getId(this.statusEffect);
            if (id == null) {
                throw new IllegalStateException("status effect perk with unregistered effect");
            }
            obj.addProperty("id", id.toString());
        }
        obj.addProperty("amplifier", this.baseAmplifier);
        obj.addProperty("duration", this.baseDuration);
        return obj;
    }

    @Override
    public void tick(ServerPlayerEntity entity) {
        if (this.statusEffect == null) {
            RPGKitMod.LOGGER.warn("StatusEffectPerk on {} with empty status effect", entity);
            return;
        }

        entity.addStatusEffect(
                new StatusEffectInstance(this.statusEffect, this.baseDuration, this.baseAmplifier,
                        false, false),
                entity
        );
    }
}
