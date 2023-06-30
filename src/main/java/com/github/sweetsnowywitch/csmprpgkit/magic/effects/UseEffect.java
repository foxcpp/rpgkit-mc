package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellBuildCondition;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
        protected Used(UseEffect effect, List<SpellReaction> appliedReactions, SpellBuildCondition.Context ctx) {
            super(effect, appliedReactions, ctx);
        }

        protected Used(UseEffect effect, JsonObject obj) {
            super(effect, obj);
        }

        public abstract ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction);

        public abstract ActionResult useOnEntity(ServerSpellCast cast, Entity entity);

        public static UseEffect.Used fromJson(JsonObject obj) {
            var effect = UseEffect.fromJson(obj.getAsJsonObject("effect"));
            return effect.usedFromJson(obj);
        }
    }

    public abstract Used use(SpellBuildCondition.Context ctx);

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
