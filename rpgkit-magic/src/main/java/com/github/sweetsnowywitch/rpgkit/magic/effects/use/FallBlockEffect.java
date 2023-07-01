package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.VectorUtils;
import com.github.sweetsnowywitch.rpgkit.magic.EffectVector;
import com.github.sweetsnowywitch.rpgkit.magic.json.BlockStatePredicate;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FallBlockEffect extends SimpleUseEffect {
    public static class Reaction extends SpellReaction {

        public final double velocity;
        public final EffectVector vector;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("velocity")) {
                this.velocity = obj.get("velocity").getAsDouble();
            } else {
                this.velocity = 0;
            }
            if (obj.has("vector")) {
                this.vector = EffectVector.valueOf(obj.get("vector").getAsString().toUpperCase());
            } else {
                this.vector = EffectVector.UP;
            }
        }

        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("velocity", this.velocity);
            obj.addProperty("vector", this.vector.name().toLowerCase());
        }
    }

    private final @Nullable BlockStatePredicate filter;
    private final double velocity;
    private final EffectVector vector;

    public FallBlockEffect(Identifier id) {
        super(id);
        this.filter = null;
        this.velocity = 0.1;
        this.vector = EffectVector.UP;
    }

    public FallBlockEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("filter")) {
            this.filter = new BlockStatePredicate(obj.get("filter"));
        } else {
            this.filter = null;
        }

        if (obj.has("velocity")) {
            this.velocity = obj.get("velocity").getAsDouble();
        } else {
            this.velocity = 0.1;
        }
        if (obj.has("vector")) {
            this.vector = EffectVector.valueOf(obj.get("vector").getAsString().toUpperCase());
        } else {
            this.vector = EffectVector.FROM_ORIGIN;
        }
    }

    @Override
    @NotNull
    protected ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        var bs = world.getBlockState(pos);
        if (bs.isAir()) {
            return ActionResult.PASS;
        }
        if (this.filter != null && !this.filter.test(bs)) {
            return ActionResult.PASS;
        }
        if (bs.hasBlockEntity()) {
            return ActionResult.PASS;
        }

        var entity = FallingBlockEntity.spawnFromBlock(world, pos, bs);

        var castDirection = VectorUtils.direction(cast.getOriginPitch(), cast.getOriginYaw());
        double velocity = this.velocity;
        Vec3d effectDirection = null;
        for (var reaction : reactions) {
            if (reaction instanceof PushEffect.Reaction r) {
                velocity += r.velocity;
                if (effectDirection != null) {
                    effectDirection = effectDirection.add(r.vector.direction(entity.getPos(), cast.getOriginPos(), castDirection));
                } else {
                    effectDirection = r.vector.direction(entity.getPos(), cast.getOriginPos(), castDirection);
                }
            }
        }
        if (effectDirection == null) {
            effectDirection = this.vector.direction(entity.getPos(), cast.getOriginPos(), castDirection);
        }
        var effectVelocityVec = effectDirection.multiply(velocity / effectDirection.length());

        entity.setVelocity(effectVelocityVec);
        entity.velocityModified = true;
        return ActionResult.SUCCESS;
    }

    @Override
    @NotNull
    protected ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions) {
        return ActionResult.PASS;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
    }
}
