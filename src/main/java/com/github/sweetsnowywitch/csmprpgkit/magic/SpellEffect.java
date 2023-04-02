package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.JSONParameters;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
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

    public void startCast(ServerSpellCast cast, ServerWorld world, Entity caster) {}
    public void endCast(ServerSpellCast cast, ServerWorld world) {}
    public abstract void onSingleEntityHit(ServerSpellCast cast, Entity entity);
    public abstract void onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir);
    public abstract void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box);
}
