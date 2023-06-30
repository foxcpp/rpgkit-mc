package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.JsonHelpers;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellBuildCondition;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class SpellEffect implements JsonHelpers.JsonSerializable {
    public final Identifier typeId;
    private final @Nullable SpellBuildCondition condition;
    protected final ImmutableList<SpellReaction> reactions;

    public static class Used<E extends SpellEffect> implements JsonHelpers.JsonSerializable {
        public final E effect;
        public final ImmutableList<SpellReaction> appliedReactions;

        public Used(E effect, List<SpellReaction> appliedReactions, SpellBuildCondition.Context ctx) {
            this.effect = effect;

            for (var r : effect.reactions) {
                if (r.appliesTo(effect) && r.shouldAdd(ctx)) {
                    appliedReactions.add(r);
                }
            }

            this.appliedReactions = ImmutableList.copyOf(appliedReactions);
        }

        public Used(E effect, JsonObject obj) {
            this.effect = effect;
            this.appliedReactions = JsonHelpers.fromJsonList(obj.getAsJsonArray("reactions"), SpellReaction::fromJson);
        }

        @Override
        @MustBeInvokedByOverriders
        public void toJson(@NotNull JsonObject obj) {
            var effectObj = new JsonObject();
            this.effect.toJson(effectObj);
            obj.add("effect", effectObj);
            obj.add("reactions", JsonHelpers.toJsonList(this.appliedReactions));
        }
    }

    protected SpellEffect(Identifier typeId) {
        this.typeId = typeId;
        this.condition = null;
        this.reactions = ImmutableList.of();
    }

    protected SpellEffect(Identifier typeId, JsonObject obj) {
        this.typeId = typeId;

        if (obj.has("condition")) {
            this.condition = SpellBuildCondition.fromJson(obj.getAsJsonObject("condition"));
        } else {
            this.condition = null;
        }

        if (obj.has("reactions")) {
            this.reactions = JsonHelpers.fromJsonList(obj.getAsJsonArray("reactions"), SpellReaction::fromJson);
        } else {
            this.reactions = ImmutableList.of();
        }
    }

    public boolean shouldAdd(SpellBuildCondition.Context ctx) {
        if (this.condition != null) {
            return this.condition.shouldAdd(ctx);
        }

        return true;
    }

    @Override
    public String toString() {
        return this.typeId.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpellEffect that = (SpellEffect) o;
        return Objects.equals(typeId, that.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeId);
    }

    @MustBeInvokedByOverriders
    public void toJson(@NotNull JsonObject obj) {
        obj.addProperty("type", this.typeId.toString());
        // Reserved for future use.
    }
}
