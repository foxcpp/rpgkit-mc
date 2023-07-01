package com.github.sweetsnowywitch.rpgkit.magic.effects;

import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AreaEffect extends SpellEffect {
    protected AreaEffect(Identifier id) {
        super(id);
    }

    protected AreaEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    @FunctionalInterface
    public interface JsonFactory {
        AreaEffect createEffectFromJSON(Identifier id, JsonObject obj);
    }

    @FunctionalInterface
    public interface AreaCollider {
        boolean containsPos(double x, double y, double z);

        static AreaCollider cube(Box box) {
            return box::contains;
        }

        static AreaCollider cylinder(double radius, double height, Vec3d baseCenter) {
            return (x, y, z) -> y >= baseCenter.y && (y - baseCenter.y) <= height &&
                    baseCenter.squaredDistanceTo(x, y, z) <= radius * radius;
        }
    }

    public static abstract class Used extends SpellEffect.Used<AreaEffect> {
        protected Used(AreaEffect effect, List<SpellReaction> globalReactions, List<SpellReaction> appliedReactions, SpellBuildCondition.Context ctx) {
            super(effect, globalReactions, appliedReactions, ctx);
        }

        protected Used(AreaEffect effect, JsonObject obj) {
            super(effect, obj);
        }

        @NotNull
        public abstract ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaCollider collider);

        public static Used fromJson(JsonObject obj) {
            var effect = AreaEffect.fromJson(obj.getAsJsonObject("effect"));
            return effect.usedFromJson(obj);
        }
    }

    @NotNull
    public abstract Used use(SpellBuildCondition.Context ctx);

    @NotNull
    public abstract Used usedFromJson(JsonObject obj);

    public static AreaEffect fromJson(JsonObject obj) {
        var type = obj.get("type");
        if (type == null) {
            throw new IllegalArgumentException("missing type field in spell effect definition");
        }
        var effectId = new Identifier(type.getAsString());
        var effect = MagicRegistries.AREA_EFFECTS.get(effectId);
        if (effect == null) {
            throw new IllegalArgumentException("unknown effect: %s".formatted(effectId.toString()));
        }
        return effect.createEffectFromJSON(effectId, obj);
    }
}
