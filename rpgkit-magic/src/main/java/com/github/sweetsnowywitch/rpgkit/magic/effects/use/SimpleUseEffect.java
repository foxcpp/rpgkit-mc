package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleUseEffect extends UseEffect {
    public SimpleUseEffect(Identifier id) {
        super(id);
    }

    public SimpleUseEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    public class Used extends UseEffect.Used {
        protected Used(SpellBuildCondition.Context ctx) {
            super(SimpleUseEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
        }

        protected Used(JsonObject obj) {
            super(SimpleUseEffect.this, obj);
        }

        @Override
        @NotNull
        public ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            return SimpleUseEffect.this.useOnBlock(cast, world, pos, direction, this.effectReactions);
        }

        @Override
        @NotNull
        public ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            return SimpleUseEffect.this.useOnEntity(cast, entity, this.effectReactions);
        }
    }

    @Override
    @NotNull
    public UseEffect.Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    @NotNull
    public UseEffect.Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @NotNull
    protected abstract ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions);

    @NotNull
    protected abstract ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions);
}
