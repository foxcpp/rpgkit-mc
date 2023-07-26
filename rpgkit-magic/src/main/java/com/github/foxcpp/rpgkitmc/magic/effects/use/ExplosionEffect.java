package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.json.FloatModifier;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

public class ExplosionEffect extends UseEffect {
    public static class Behavior extends ExplosionBehavior {
        float blastResistanceDecrease;
        boolean destroyBlocks;

        Behavior(float blastResistanceDecrease, boolean destroyBlocks) {
            this.blastResistanceDecrease = blastResistanceDecrease;
            this.destroyBlocks = destroyBlocks;
        }

        @Override
        public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
            return super.getBlastResistance(explosion, world, pos, blockState, fluidState).
                    map((v) -> Math.max(0, v - this.blastResistanceDecrease));
        }

        @Override
        public boolean canDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float power) {
            return destroyBlocks;
        }
    }

    public static class Reaction extends SpellReaction {
        protected final FloatModifier blastResistanceDecrease;
        protected final boolean dropBlocks;
        protected final boolean breakBlocks;
        protected final FloatModifier powerMultiplier;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("blast_resistance_decrease")) {
                this.blastResistanceDecrease = new FloatModifier(obj.get("blast_resistance_decrease"));
            } else {
                this.blastResistanceDecrease = FloatModifier.NOOP;
            }
            this.dropBlocks = obj.has("drop_blocks") && obj.get("drop_blocks").getAsBoolean();
            this.breakBlocks = !obj.has("break_blocks") || obj.get("break_blocks").getAsBoolean();
            if (obj.has("power")) {
                this.powerMultiplier = new FloatModifier(obj.get("power"));
            } else {
                this.powerMultiplier = FloatModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof ExplosionEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("blast_resistance_decrease", this.blastResistanceDecrease.toJson());
            obj.addProperty("drop_blocks", this.dropBlocks);
            obj.addProperty("break_blocks", this.breakBlocks);
            obj.add("power", this.powerMultiplier.toJson());
        }
    }

    public class Used extends UseEffect.Used {
        protected final float blastResistanceDecrease;
        protected final boolean dropBlocks;
        protected final boolean breakBlocks;
        protected final float powerMultiplier;

        protected Used(SpellBuildCondition.Context ctx) {
            super(ExplosionEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            var blastResistanceDecrease = ExplosionEffect.this.blastResistanceDecrease;
            var breakBlocks = ExplosionEffect.this.breakBlocks;
            var dropBlocks = ExplosionEffect.this.dropBlocks;
            var power = ExplosionEffect.this.powerMultiplier;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    blastResistanceDecrease = r.blastResistanceDecrease.applyMultiple(blastResistanceDecrease, ctx.stackSize);
                    breakBlocks = breakBlocks || r.breakBlocks;
                    dropBlocks = dropBlocks || r.dropBlocks;
                    power = r.powerMultiplier.applyMultiple(power, ctx.stackSize);
                }
            }
            this.blastResistanceDecrease = blastResistanceDecrease;
            this.dropBlocks = dropBlocks;
            this.breakBlocks = breakBlocks;
            this.powerMultiplier = power;
        }

        protected Used(JsonObject obj) {
            super(ExplosionEffect.this, obj);
            this.blastResistanceDecrease = obj.get("blast_resistance_decrease").getAsFloat();
            this.dropBlocks = obj.has("drop_blocks") && obj.get("drop_blocks").getAsBoolean();
            this.breakBlocks = !obj.has("break_blocks") || obj.get("break_blocks").getAsBoolean();
            this.powerMultiplier = obj.get("power").getAsFloat();
        }

        @Override
        public @NotNull ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            var caster = cast.getCaster(world);

            DamageSource damageSource;
            if (caster instanceof LivingEntity le) {
                damageSource = DamageSource.explosion(le);
            } else {
                damageSource = DamageSource.MAGIC;
            }

            RPGKitMagicMod.LOGGER.debug("Created spell explosion at {} with power {} (break blocks = {}, blast resistance - {})",
                    pos, this.powerMultiplier, this.breakBlocks, this.blastResistanceDecrease);

            world.createExplosion(caster, damageSource, new Behavior(ExplosionEffect.this.blastResistanceDecrease, this.breakBlocks),
                    pos.getX(), pos.getY(), pos.getZ(), this.powerMultiplier, false, Explosion.DestructionType.DESTROY);
            return ActionResult.SUCCESS;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("blast_resistance_decrease", this.blastResistanceDecrease);
            obj.addProperty("drop_blocks", this.dropBlocks);
            obj.addProperty("break_blocks", this.breakBlocks);
            obj.addProperty("power", this.powerMultiplier);
        }
    }

    protected final float blastResistanceDecrease;
    protected final boolean dropBlocks;
    protected final boolean breakBlocks;

    protected final float powerMultiplier;

    public ExplosionEffect(Identifier id) {
        super(id);
        this.blastResistanceDecrease = 0f;
        this.dropBlocks = false;
        this.breakBlocks = true;
        this.powerMultiplier = 0.05f;
    }

    public ExplosionEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("blast_resistance_decrease")) {
            this.blastResistanceDecrease = obj.get("blast_resistance_decrease").getAsFloat();
        } else {
            this.blastResistanceDecrease = 0f;
        }
        this.dropBlocks = obj.has("drop_blocks") && obj.get("drop_blocks").getAsBoolean();
        this.breakBlocks = !obj.has("break_blocks") || obj.get("break_blocks").getAsBoolean();
        if (obj.has("power")) {
            this.powerMultiplier = obj.get("power").getAsFloat();
        } else {
            this.powerMultiplier = 0.4f;
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
        obj.addProperty("blast_resistance_decrease", this.blastResistanceDecrease);
        obj.addProperty("drop_blocks", this.dropBlocks);
        obj.addProperty("break_blocks", this.breakBlocks);
        obj.addProperty("power", this.powerMultiplier);
    }
}
