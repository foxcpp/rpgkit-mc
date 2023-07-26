package com.github.foxcpp.rpgkitmc.magic.effects;

import com.github.foxcpp.rpgkitmc.JsonHelpers;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class SpellEffect implements JsonHelpers.JsonSerializable {
    public final Identifier typeId;
    private final @Nullable SpellBuildCondition condition;
    protected final ImmutableList<SpellReaction> reactions;

    public static class Used<E extends SpellEffect> implements JsonHelpers.JsonSerializable {
        public final E effect;
        public final ImmutableList<SpellReaction> effectReactions;
        protected final List<SpellReaction> globalReactions;

        public Used(E effect, List<SpellReaction> globalReactions, List<SpellReaction> effectReactions, SpellBuildCondition.Context ctx) {
            this.effect = effect;
            this.globalReactions = new ArrayList<>(globalReactions.size());

            for (var r : globalReactions) {
                if (r.shouldAdd(ctx)) {
                    this.globalReactions.add(r);
                }
            }

            for (var r : effect.reactions) {
                if (r.type.equals(SpellReaction.Type.FORM) || !r.appliesTo(effect)) {
                    this.globalReactions.add(r);
                } else if (r.shouldAdd(ctx)) {
                    effectReactions.add(r);
                }
            }

            this.effectReactions = ImmutableList.copyOf(effectReactions);
        }

        public Used(E effect, JsonObject obj) {
            this.effect = effect;
            this.globalReactions = JsonHelpers.fromJsonList(obj.getAsJsonArray("global_reactions"), SpellReaction::fromJson);
            this.effectReactions = JsonHelpers.fromJsonList(obj.getAsJsonArray("effect_reactions"), SpellReaction::fromJson);
        }

        public @Unmodifiable List<SpellReaction> getGlobalReactions() {
            return Collections.unmodifiableList(this.globalReactions);
        }

        @Override
        @MustBeInvokedByOverriders
        public void toJson(@NotNull JsonObject obj) {
            var effectObj = new JsonObject();
            this.effect.toJson(effectObj);
            obj.add("effect", effectObj);
            obj.add("global_reactions", JsonHelpers.toJsonList(this.globalReactions));
            obj.add("effect_reactions", JsonHelpers.toJsonList(this.effectReactions));
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

    public ImmutableList<SpellReaction> getPotentialReactions() {
        return this.reactions;
    }

    @MustBeInvokedByOverriders
    public void toJson(@NotNull JsonObject obj) {
        obj.addProperty("type", this.typeId.toString());
        // Reserved for future use.
    }
}
