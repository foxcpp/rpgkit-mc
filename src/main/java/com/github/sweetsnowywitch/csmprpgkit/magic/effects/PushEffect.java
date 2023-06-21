package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

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
        COUNTERCLOCKWISE,
        UP,
        DOWN,
        ZERO;

        public Vec3d direction(Vec3d target, Vec3d origin) {
            return switch (this) {
                case TOWARDS_ORIGIN -> origin.subtract(target).normalize();
                case FROM_ORIGIN -> target.subtract(origin).normalize();
                case COUNTERCLOCKWISE -> target.subtract(origin).crossProduct(new Vec3d(0, -1, 0));
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
        double velocity = this.velocity;
        Vec3d direction = null;
        for (var reaction : cast.getReactions()) {
            if (reaction instanceof Reaction r) {
                velocity += r.velocity;
                if (direction != null) {
                    direction = direction.add(r.vector.direction(entity.getPos(), cast.getOriginPos()));
                } else {
                    direction = r.vector.direction(entity.getPos(), cast.getOriginPos());
                }
            }
        }
        if (direction == null) {
            direction = this.vector.direction(entity.getPos(), cast.getOriginPos());
        }
        var velocityVec = direction.multiply(velocity / direction.length());
        var currentVelocity = entity.getVelocity();

        entity.velocityDirty = true;
        entity.velocityModified = true;
        if (currentVelocity.y + velocityVec.y > 0) {
            entity.setVelocity(
                    currentVelocity.x + velocityVec.x,
                    Math.max(1, (currentVelocity.y + velocityVec.y) / 4),
                    currentVelocity.z + velocityVec.z);
        } else {
            entity.setVelocity(
                    currentVelocity.x + velocityVec.x,
                    currentVelocity.y + velocityVec.y,
                    currentVelocity.z + velocityVec.z);
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
