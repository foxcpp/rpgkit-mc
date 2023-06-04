package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class SpellEffect {
    public interface Factory {
        SpellEffect createDefaultEffect(Identifier id);

        SpellEffect createEffectFromJSON(Identifier id, JsonObject obj);
    }

    public static Factory factoryFor(Function<Identifier, SpellEffect> def, BiFunction<Identifier, JsonObject, SpellEffect> json) {
        return new Factory() {
            @Override
            public SpellEffect createDefaultEffect(Identifier id) {
                return def.apply(id);
            }

            @Override
            public SpellEffect createEffectFromJSON(Identifier id, JsonObject obj) {
                return json.apply(id, obj);
            }
        };
    }

    public final Identifier id;
    private final @Nullable SpellBuildCondition condition;

    protected SpellEffect(Identifier id) {
        this.id = id;
        this.condition = null;
    }

    protected SpellEffect(Identifier id, JsonObject obj) {
        this.id = id;

        if (obj.has("condition")) {
            this.condition = SpellBuildCondition.fromJson(obj.getAsJsonObject("condition"));
        } else {
            this.condition = null;
        }
    }

    public boolean shouldAdd(SpellBuilder builder, @Nullable SpellElement source) {
        if (this.condition != null) {
            return this.condition.shouldAdd(builder, source);
        }

        return true;
    }

    public void startCast(ServerSpellCast cast, ServerWorld world, Entity caster) {
    }

    public void endCast(ServerSpellCast cast, ServerWorld world) {
    }

    public abstract boolean onSingleEntityHit(ServerSpellCast cast, Entity entity);

    public abstract boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir);

    public abstract void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box);

    @Override
    public String toString() {
        return this.id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpellEffect that = (SpellEffect) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @MustBeInvokedByOverriders
    public void toJson(JsonObject obj) {
        obj.addProperty("type", this.id.toString());
        // Reserved for future use.
    }

    public static SpellEffect fromJson(JsonObject obj) {
        var type = obj.get("type");
        if (type == null) {
            throw new IllegalArgumentException("missing type field in spell effect definition");
        }
        var effectId = new Identifier(type.getAsString());
        var effect = ModRegistries.SPELL_EFFECTS.get(effectId);
        if (effect == null) {
            throw new IllegalArgumentException("unknown effect: %s".formatted(effectId.toString()));
        }
        return effect.createEffectFromJSON(effectId, obj);
    }
}
