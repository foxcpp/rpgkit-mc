package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.VectorUtils;
import com.github.sweetsnowywitch.rpgkit.magic.EffectVector;
import com.github.sweetsnowywitch.rpgkit.magic.ProtectionBreakingEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.SpellEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.events.MagicEntityEvents;
import com.github.sweetsnowywitch.rpgkit.magic.json.DoubleModifier;
import com.github.sweetsnowywitch.rpgkit.magic.json.FloatModifier;
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
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PushEffect extends UseEffect {
    public static class Reaction extends SpellReaction {
        public final DoubleModifier velocity;
        public final FloatModifier magicStrength;
        public final EffectVector vector;
        public final boolean disregardCurrentVelocity;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("velocity")) {
                this.velocity = new DoubleModifier(obj.get("velocity"));
            } else {
                this.velocity = DoubleModifier.NOOP;
            }
            if (obj.has("magic_strength")) {
                this.magicStrength = new FloatModifier(obj.get("magic_strength"));
            } else {
                this.magicStrength = FloatModifier.NOOP;
            }
            if (obj.has("vector")) {
                this.vector = EffectVector.valueOf(obj.get("vector").getAsString().toUpperCase());
            } else {
                this.vector = EffectVector.FROM_ORIGIN;
            }
            this.disregardCurrentVelocity = obj.has("disregard_current_velocity") &&
                    obj.get("disregard_current_velocity").getAsBoolean();
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof PushEffect;
        }

        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("velocity", this.velocity.toJson());
            obj.addProperty("vector", this.vector.name().toLowerCase());
            obj.addProperty("disregard_current_velocity", this.disregardCurrentVelocity);
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
    private final float magicStrength;
    private final EffectVector vector;
    public final boolean disregardCurrentVelocity;

    public PushEffect(Identifier id) {
        super(id);
        this.velocity = 3;
        this.magicStrength = 1;
        this.vector = EffectVector.FROM_ORIGIN;
        this.disregardCurrentVelocity = false;
    }

    public PushEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("velocity")) {
            this.velocity = obj.get("velocity").getAsDouble();
        } else {
            this.velocity = 3;
        }
        if (obj.has("magic_strength")) {
            this.magicStrength = obj.get("magic_strength").getAsFloat();
        } else {
            this.magicStrength = 1;
        }
        if (obj.has("vector")) {
            this.vector = EffectVector.valueOf(obj.get("vector").getAsString().toUpperCase());
        } else {
            this.vector = EffectVector.FROM_ORIGIN;
        }
        this.disregardCurrentVelocity = obj.has("disregard_current_velocity") &&
                obj.get("disregard_current_velocity").getAsBoolean();
    }

    public class Used extends UseEffect.Used implements ProtectionBreakingEffect {
        public final double velocity;
        public final float magicStrength;
        public final boolean disregardCurrentVelocity;

        protected Used(SpellBuildCondition.Context ctx) {
            super(PushEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);

            var magStrength = PushEffect.this.magicStrength;
            var velocity = PushEffect.this.velocity;
            boolean disregardCurrentVelocity = PushEffect.this.disregardCurrentVelocity;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    velocity = r.velocity.applyMultiple(velocity, ctx.stackSize);
                    magStrength = r.magicStrength.applyMultiple(magStrength, ctx.stackSize);
                    disregardCurrentVelocity = disregardCurrentVelocity || r.disregardCurrentVelocity;
                }
            }
            for (var reaction : this.getGlobalReactions()) {
                if (reaction instanceof Reaction r) {
                    velocity = r.velocity.applyMultiple(velocity, ctx.stackSize);
                    magStrength = r.magicStrength.applyMultiple(magStrength, ctx.stackSize);
                    disregardCurrentVelocity = disregardCurrentVelocity || r.disregardCurrentVelocity;
                }
            }
            this.velocity = velocity;
            this.magicStrength = magStrength;
            this.disregardCurrentVelocity = disregardCurrentVelocity;
        }

        protected Used(JsonObject obj) {
            super(PushEffect.this, obj);
            this.velocity = obj.get("velocity").getAsFloat();
            this.magicStrength = obj.get("magic_strength").getAsFloat();
            this.disregardCurrentVelocity = obj.get("disregard_current_velocity").getAsBoolean();
        }

        @Override
        public @NotNull ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            return ActionResult.PASS;
        }

        @Override
        public @NotNull ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            var castDirection = VectorUtils.direction(cast.getOriginPitch(), cast.getOriginYaw());
            Vec3d effectDirection = null;
            for (var reaction : reactions) {
                if (reaction instanceof Reaction r) {
                    if (effectDirection != null) {
                        effectDirection = effectDirection.add(r.vector.direction(entity.getPos(), cast.getOriginPos(), castDirection));
                    } else {
                        effectDirection = r.vector.direction(entity.getPos(), cast.getOriginPos(), castDirection);
                    }
                }
            }
            if (effectDirection == null) {
                effectDirection = PushEffect.this.vector.direction(entity.getPos(), cast.getOriginPos(), castDirection);
            }

            var effectiveVelocity = MagicEntityEvents.MOVE.invoker().onEntityMagicMoved(cast, this, entity, this.velocity);
            if (effectiveVelocity.getResult().equals(ActionResult.FAIL) || effectiveVelocity.getResult().equals(ActionResult.CONSUME)) {
                return effectiveVelocity.getResult();
            }
            var effectVelocityVec = effectDirection.multiply(effectiveVelocity.getValue() / effectDirection.length());

            var currentVelocity = entity.getVelocity();
            if (disregardCurrentVelocity) {
                currentVelocity = Vec3d.ZERO;
            }

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
            return ActionResult.SUCCESS;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("velocity", this.velocity);
            obj.addProperty("magic_strength", this.magicStrength);
            obj.addProperty("disregard_current_velocity", this.disregardCurrentVelocity);
        }

        @Override
        public FloatModifier calculateEffectReduction(ServerSpellCast cast, float protectionStrength) {
            return null;
        }

        @Override
        public boolean willDissolveProtection(ServerSpellCast cast, float protectionStrength) {
            return false;
        }
    }

    @Override
    public UseEffect.@NotNull Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public UseEffect.@NotNull Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("velocity", this.velocity);
        obj.addProperty("vector", this.vector.name().toLowerCase());
    }
}
