package com.github.foxcpp.rpgkitmc.magic.effects;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
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
        public ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            return this.useOnBlock(cast, (ServerWorld) entity.getWorld(), entity.getBlockPos(), Direction.UP);
        }

        public static UseEffect.Used fromJson(JsonObject obj) {
            var effect = UseEffect.fromJson(obj.getAsJsonObject("effect"));
            return effect.usedFromJson(obj);
        }
    }

    protected UseEffect(Identifier id) {
        super(id);
    }

    protected UseEffect(Identifier id, JsonObject obj) {
        super(id, obj);
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
