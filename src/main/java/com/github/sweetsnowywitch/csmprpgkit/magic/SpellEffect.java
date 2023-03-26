package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.JSONParameters;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class SpellEffect implements JSONParameters<SpellEffect> {
    @Override
    public SpellEffect withParametersFromJSON(JsonObject jsonObject) {
        return this;
    }

    @Override
    public JsonObject parametersToJSON() {
        return new JsonObject();
    }

    public @Nullable SpellReaction reactionType(Identifier id) {
        return null;
    }

    public void startCast(SpellCast cast, ImmutableList<SpellReaction> reactions) {}
    public void endCast(SpellCast cast, ImmutableList<SpellReaction> reactions) {}
    public abstract void onSingleEntityHit(SpellCast cast, Entity entity, ImmutableList<SpellReaction> reactions);
    public abstract void onSingleBlockHit(SpellCast cast, BlockPos pos, Direction dir, ImmutableList<SpellReaction> reactions);
    public abstract void onAreaHit(SpellCast cast, Vec3d position, ImmutableList<SpellReaction> reactions);
    public void onSelfHit(SpellCast cast, ImmutableList<SpellReaction> reactions) {
        this.onSingleEntityHit(cast, cast.getCaster(), reactions);
    }
}
