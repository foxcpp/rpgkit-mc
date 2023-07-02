package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
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

import java.util.List;
import java.util.Optional;

public class ExplosionEffect extends SimpleUseEffect {
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
        if (obj.has("power_multiplier")) {
            this.powerMultiplier = obj.get("power_multiplier").getAsFloat();
        } else {
            this.powerMultiplier = 0.4f;
        }
    }

    @Override
    public @NotNull ActionResult useOnBlock(ServerSpellCast cast, UseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        var caster = cast.getCaster(world);

        DamageSource damageSource;
        if (caster instanceof LivingEntity le) {
            damageSource = DamageSource.explosion(le);
        } else {
            damageSource = DamageSource.MAGIC;
        }

        var power = ExplosionEffect.this.powerMultiplier;

        RPGKitMagicMod.LOGGER.debug("Created spell explosion at {} with power {} (break blocks = {}, blast resistance - {})",
                pos, power, breakBlocks, blastResistanceDecrease);

        world.createExplosion(caster, damageSource, new Behavior(ExplosionEffect.this.blastResistanceDecrease, ExplosionEffect.this.breakBlocks),
                pos.getX(), pos.getY(), pos.getZ(), power, false, Explosion.DestructionType.DESTROY);
        return ActionResult.SUCCESS;
    }

    @Override
    public @NotNull ActionResult useOnEntity(ServerSpellCast cast, UseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        return ActionResult.PASS;
    }
}
