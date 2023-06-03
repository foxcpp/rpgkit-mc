package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * SpellReaction is a base class for spell "reaction". Reaction
 * is a spell cast element that changes how its form or effect works, typically
 * by changing its parameters.
 */
public abstract class SpellReaction {
    public interface Factory {
        @Nullable SpellReaction createDefaultReaction(Identifier id);

        @Nullable SpellReaction createReactionFromJson(Identifier id, JsonObject obj);
    }

    public static Factory factoryFor(Function<Identifier, SpellReaction> def, BiFunction<Identifier, JsonObject, SpellReaction> json) {
        return new Factory() {
            public @Nullable SpellReaction createDefaultReaction(Identifier id) {
                return def.apply(id);
            }

            public @Nullable SpellReaction createReactionFromJson(Identifier id, JsonObject obj) {
                return json.apply(id, obj);
            }
        };
    }

    public final Identifier id;
    private final ImmutableMap<String, Float> costMultipliers;
    private final ImmutableMap<String, Float> costTerms;
    private final @Nullable SpellBuildCondition condition;

    public boolean appliesTo(SpellEffect effect) {
        return false;
    }

    public boolean appliesTo(SpellForm form) {
        return false;
    }

    public SpellReaction(Identifier id) {
        this.id = id;
        this.costMultipliers = ImmutableMap.of();
        this.costTerms = ImmutableMap.of();
        this.condition = null;
    }

    public SpellReaction(Identifier id, JsonObject obj) {
        this.id = id;

        ImmutableMap.Builder<String, Float> costMultipliers = ImmutableMap.builder();
        ImmutableMap.Builder<String, Float> costTerms = ImmutableMap.builder();

        var costs = obj.getAsJsonObject("costs");
        if (costs != null) {
            for (var entry : costs.entrySet()) {
                var values = entry.getValue().getAsJsonObject();
                if (values.has("add")) {
                    costTerms.put(entry.getKey(), values.get("add").getAsFloat());
                }
                if (values.has("mul")) {
                    costMultipliers.put(entry.getKey(), values.get("mul").getAsFloat());
                }
            }
        }

        this.costTerms = costTerms.build();
        this.costMultipliers = costMultipliers.build();

        if (obj.has("condition")) {
            this.condition = SpellBuildCondition.fromJson(obj.getAsJsonObject("condition"));
        } else {
            this.condition = null;
        }
    }

    public boolean shouldAdd(SpellBuilder builder, @Nullable SpellElement source) {
        if (this.condition != null) {
            return this.condition.shouldAdd(builder, source);
        }

        return true;
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

    @MustBeInvokedByOverriders
    public void toJson(@NotNull JsonObject obj) {
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

    @Override
    public String toString() {
        return this.id.toString();
    }
}
