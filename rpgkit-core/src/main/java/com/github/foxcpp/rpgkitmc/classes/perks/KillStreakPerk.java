package com.github.foxcpp.rpgkitmc.classes.perks;

import com.github.foxcpp.rpgkitmc.classes.Perk;
import com.google.gson.JsonObject;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class KillStreakPerk extends Perk {
    public static final int BASE_MODIFIER = Integer.MAX_VALUE;
    public static final int BASE_PENALTY = 0;
    public static final int BASE_MAX_STREAK = 0;

    private int killStreak = 0;

    private final EntityAttribute attribute;
    private final boolean onlyPlayerKill;
    private final float modifier;
    private final int penalty;
    private final int maxStreak;

    public KillStreakPerk(Identifier typeId) {
        super(typeId);
        this.attribute = null;
        this.onlyPlayerKill = false;
        this.modifier = BASE_MODIFIER;
        this.penalty = BASE_PENALTY;
        this.maxStreak = BASE_MAX_STREAK;
    }

    public KillStreakPerk(Identifier typeId, @Nullable EntityAttribute attribute, boolean onlyPlayerKill, float modifier, int penalty, int maxStreak) {
        super(typeId);
        this.attribute = attribute;
        this.onlyPlayerKill = onlyPlayerKill;
        this.modifier = modifier;
        this.penalty = penalty;
        this.maxStreak = maxStreak;
    }

    @Override
    public Perk withParametersFromJSON(JsonObject obj) {
        var attribute = this.attribute;
        if (obj.has("id")) {
            var id = new Identifier(obj.get("id").getAsString());
            attribute = Registries.ATTRIBUTE.get(id);
            if (attribute == null) {
                throw new IllegalStateException("unknown attribute");
            }
        }

        var playerKill = this.onlyPlayerKill;
        if (obj.has("playerKill")) {
            playerKill = obj.get("playerKill").getAsBoolean();
        }


        var modifier = this.modifier;
        if (obj.has("modifier")) {
            modifier = obj.get("modifier").getAsFloat();
        }

        var penalty = this.penalty;
        if (obj.has("penalty")) {
            penalty = obj.get("penalty").getAsInt();
        }

        var maxStreak = this.maxStreak;
        if (obj.has("maxStreak")) {
            maxStreak = obj.get("maxStreak").getAsInt();
        }
        return new KillStreakPerk(this.typeId, attribute, playerKill, modifier, penalty, maxStreak);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        if (this.attribute != null) {
            var id = Registries.ATTRIBUTE.getId(this.attribute);
            if (id == null) {
                throw new IllegalStateException("on kill perk with unregistered attribute");
            }
            obj.addProperty("id", id.toString());
        }
        obj.addProperty("playerKill", this.onlyPlayerKill);
        obj.addProperty("modifier", this.modifier);
        obj.addProperty("penalty", this.penalty);
        obj.addProperty("maxStreak", this.maxStreak);
        return obj;
    }

    public EntityAttribute getAttribute() {
        return attribute;
    }

    public boolean isOnlyPlayerKill() {
        return onlyPlayerKill;
    }

    public float getModifier() {
        return modifier;
    }

    public int getPenalty() {
        return penalty;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public int getKillStreak() {
        return killStreak;
    }

    public void setKillStreak(int killStreak) {
        this.killStreak = killStreak;
    }
}
