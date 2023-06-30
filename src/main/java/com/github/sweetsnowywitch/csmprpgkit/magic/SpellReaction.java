package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.JsonHelpers;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.SpellEffect;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * SpellReaction is a base class for spell "reaction". Reaction
 * is a spell cast element that changes how its form or effect works, typically
 * by changing its parameters.
 */
public abstract class SpellReaction implements JsonHelpers.JsonSerializable {
    @FunctionalInterface
    public interface JsonFactory {
        SpellReaction createReactionFromJson(JsonObject obj);
    }

    public final Identifier targetId;
    private final @Nullable SpellBuildCondition condition;

    public boolean appliesTo(SpellEffect effect) {
        return false;
    }

    public boolean appliesTo(SpellForm form) {
        return false;
    }

    protected SpellReaction(JsonObject obj) {
        this.targetId = new Identifier(obj.get("for").getAsString());

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

        if (obj.has("condition")) {
            this.condition = SpellBuildCondition.fromJson(obj.getAsJsonObject("condition"));
        } else {
            this.condition = null;
        }
    }

    public boolean shouldAdd(SpellBuildCondition.Context ctx) {
        if (this.condition != null) {
            return this.condition.shouldAdd(ctx);
        }

        return true;
    }

    @MustBeInvokedByOverriders
    public void toJson(@NotNull JsonObject obj) {
        obj.addProperty("for", this.targetId.toString());
    }

    public static SpellReaction fromJson(JsonObject obj) {
        var type = obj.get("for");
        if (type == null) {
            throw new IllegalArgumentException("missing type field in spell effect definition");
        }
        var id = new Identifier(type.getAsString());
        var reaction = MagicRegistries.REACTIONS.get(id);
        if (reaction == null) {
            throw new IllegalArgumentException("unknown reaction: %s".formatted(id.toString()));
        }
        return reaction.createReactionFromJson(obj);
    }
}
