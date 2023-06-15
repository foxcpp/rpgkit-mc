package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.Optional;

public class ExplosionEffect extends SpellEffect {
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

    private final float blastResistanceDecrease;
    private final boolean dropBlocks;
    private final boolean breakBlocks;

    private final float powerMultiplier;

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
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        this.onAreaHit(cast, (ServerWorld) entity.getWorld(), Box.of(entity.getPos(), 4, 4, 4));
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        this.onAreaHit(cast, world, Box.from(Vec3d.ofCenter(pos)));
        return false;
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        var caster = cast.getCaster(world);

        DamageSource damageSource;
        if (caster instanceof LivingEntity le) {
            damageSource = DamageSource.explosion(le);
        } else {
            damageSource = DamageSource.MAGIC;
        }

        var center = box.getCenter();
        var power = this.powerMultiplier * Math.min(box.getXLength(), box.getZLength());

        RPGKitMod.LOGGER.debug("Created spell explosion at {} with power {} (break blocks = {}, blast resistance - {})", center, power, breakBlocks, blastResistanceDecrease);

        world.createExplosion(caster, damageSource, new Behavior(this.blastResistanceDecrease, this.breakBlocks),
                center.x, center.y, center.z, (float) power, false, Explosion.DestructionType.DESTROY);
    }
}
