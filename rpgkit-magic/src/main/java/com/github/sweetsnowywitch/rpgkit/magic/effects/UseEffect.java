package com.github.sweetsnowywitch.rpgkit.magic.effects;

import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
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

import java.util.List;

public abstract class UseEffect extends SpellEffect {
    protected UseEffect(Identifier id) {
        super(id);
    }

    protected UseEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    @FunctionalInterface
    public interface JsonFactory {
        UseEffect createEffectFromJSON(Identifier id, JsonObject obj);
    }

    public static abstract class Used extends SpellEffect.Used<UseEffect> {
        protected Used(UseEffect effect, List<SpellReaction> globalReactions, List<SpellReaction> appliedReactions, SpellBuildCondition.Context ctx) {
            super(effect, globalReactions, appliedReactions, ctx);
        }

        protected Used(UseEffect effect, JsonObject obj) {
            super(effect, obj);
        }

        @NotNull
        public abstract ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction);

        @NotNull
        public abstract ActionResult useOnEntity(ServerSpellCast cast, Entity entity);

        public static UseEffect.Used fromJson(JsonObject obj) {
            var effect = UseEffect.fromJson(obj.getAsJsonObject("effect"));
            return effect.usedFromJson(obj);
        }
    }

    @NotNull
    public abstract Used use(SpellBuildCondition.Context ctx);

    @NotNull
    public abstract Used usedFromJson(JsonObject obj);

    public static UseEffect fromJson(JsonObject obj) {
        var type = obj.get("type");
        if (type == null) {
            throw new IllegalArgumentException("missing type field in spell effect definition");
        }
        var effectId = new Identifier(type.getAsString());
        var effect = MagicRegistries.USE_EFFECTS.get(effectId);
        if (effect == null) {
            throw new IllegalArgumentException("unknown effect: %s".formatted(effectId.toString()));
        }
        return effect.createEffectFromJSON(effectId, obj);
    }
}
