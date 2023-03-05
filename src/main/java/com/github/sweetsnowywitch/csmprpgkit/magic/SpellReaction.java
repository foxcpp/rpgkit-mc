package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.JSONParameters;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class SpellReaction implements JSONParameters<SpellReaction> {
    private ImmutableMap<String, Float> costMultipliers;
    private ImmutableMap<String, Float> costTerms;

    public boolean appliesTo(SpellEffect effect) {
        return false;
    }
    public boolean appliesTo(SpellForm form) {
        return false;
    }

    public SpellReaction() {
        this.costMultipliers = ImmutableMap.of();
        this.costTerms = ImmutableMap.of();
    }

    public SpellReaction(ImmutableMap<String, Float> costMultipliers, ImmutableMap<String, Float> costTerms) {
        this.costMultipliers = costMultipliers;
        this.costTerms = costTerms;
    }

    public float getCostMultiplier(String key) {
        return Objects.requireNonNull(costMultipliers.getOrDefault(key, (float) 1));
    }

    public float getCostTerm(String key) {
        return Objects.requireNonNull(costTerms.getOrDefault(key, (float) 0));
    }

    public final float applyCost(String key, float cost) {
        return cost * this.getCostMultiplier(key) + this.getCostTerm(key);
    }

    protected void populateFromJson(@NotNull JsonObject obj) {
        ImmutableMap.Builder<String, Float> costMultipliers = ImmutableMap.builder();
        ImmutableMap.Builder<String, Float> costTerms = ImmutableMap.builder();

        var costs = obj.getAsJsonObject("costs");
        for (var entry : costs.entrySet()) {
            var values = entry.getValue().getAsJsonObject();
            if (values.has("add")) {
                costTerms.put(entry.getKey(), values.get("add").getAsFloat());
            }
            if (values.has("mul")) {
                costMultipliers.put(entry.getKey(), values.get("mul").getAsFloat());
            }
        }

        this.costTerms = costTerms.build();
        this.costMultipliers = costMultipliers.build();

    }

    protected void writeToJson(@NotNull JsonObject obj) {
        if (obj.has("costs")) {
            obj.remove("costs");
        }
        var costsObj = new JsonObject();
        for (var entry : this.costMultipliers.entrySet()) {
            if (!costsObj.has(entry.getKey())) {
                costsObj.add(entry.getKey(), new JsonObject());
            }
            costsObj.getAsJsonObject(entry.getKey()).addProperty("mul", entry.getValue());
        }
        for (var entry : this.costTerms.entrySet()) {
            if (!costsObj.has(entry.getKey())) {
                costsObj.add(entry.getKey(), new JsonObject());
            }
            costsObj.getAsJsonObject(entry.getKey()).addProperty("add", entry.getValue());
        }
        obj.add("costs", costsObj);
    }
}
