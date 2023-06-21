package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.MovementUtils;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class PushEffect extends SpellEffect {
    public enum EffectVector {
        TOWARDS_ORIGIN,
        FROM_ORIGIN,
        FORWARD,
        COUNTERCLOCKWISE,
        CLOCKWISE,
        UP,
        DOWN,
        ZERO;

        public Vec3d direction(Vec3d target, Vec3d origin, Vec3d direction) {
            return switch (this) {
                case TOWARDS_ORIGIN -> origin.subtract(target);
                case FROM_ORIGIN -> target.subtract(origin);
                case FORWARD -> direction;
                case COUNTERCLOCKWISE -> target.subtract(origin).crossProduct(new Vec3d(0, -1, 0));
                case CLOCKWISE -> target.subtract(origin).crossProduct(new Vec3d(0, 1, 0));
                case UP -> new Vec3d(0, 0.5, 0);
                case DOWN -> new Vec3d(0, -1, 0);
                case ZERO -> Vec3d.ZERO;
            };
        }
    }

    public static class Reaction extends SpellReaction {

        public final double velocity;
        public final EffectVector vector;

        public Reaction(Identifier id) {
            super(id);
            this.velocity = 0;
            this.vector = EffectVector.FROM_ORIGIN;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id);
            if (obj.has("velocity")) {
                this.velocity = obj.get("velocity").getAsDouble();
            } else {
                this.velocity = 0;
            }
            if (obj.has("vector")) {
                this.vector = EffectVector.valueOf(obj.get("vector").getAsString().toUpperCase());
            } else {
                this.vector = EffectVector.FROM_ORIGIN;
            }
        }

        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("velocity", this.velocity);
            obj.addProperty("vector", this.vector.name().toLowerCase());
        }

        @Override
        public String toString() {
            return "PushEffect.Reaction{" +
                    "velocity=" + velocity +
                    ", vector=" + vector +
                    '}';
        }
    }

    private final double velocity;
    private final EffectVector vector;

    public PushEffect(Identifier id) {
        super(id);
        this.velocity = 3;
        this.vector = EffectVector.FROM_ORIGIN;
    }

    public PushEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("velocity")) {
            this.velocity = obj.get("velocity").getAsDouble();
        } else {
            this.velocity = 3;
        }
        if (obj.has("vector")) {
            this.vector = EffectVector.valueOf(obj.get("vector").getAsString().toUpperCase());
        } else {
            this.vector = EffectVector.FROM_ORIGIN;
        }
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        var castDirection = MovementUtils.rotationVector(cast.getOriginPitch(), cast.getOriginYaw());
        double velocity = this.velocity;
        Vec3d effectDirection = null;
        for (var reaction : cast.getReactions()) {
            if (reaction instanceof Reaction r) {
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
        var currentVelocity = entity.getVelocity();

        entity.velocityDirty = true;
        entity.velocityModified = true;
        if (currentVelocity.y + effectVelocityVec.y > 0) {
            entity.setVelocity(
                    currentVelocity.x + effectVelocityVec.x,
                    Math.max(1, (currentVelocity.y + effectVelocityVec.y) / 4),
                    currentVelocity.z + effectVelocityVec.z);
        } else {
            entity.setVelocity(
                    currentVelocity.x + effectVelocityVec.x,
                    currentVelocity.y + effectVelocityVec.y,
                    currentVelocity.z + effectVelocityVec.z);
        }
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        // none
        return false;
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        // none
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("velocity", this.velocity);
        obj.addProperty("vector", this.vector.name().toLowerCase());
    }
}
