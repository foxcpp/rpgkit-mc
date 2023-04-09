package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.effects.ModStatusEffects;
import com.github.sweetsnowywitch.csmprpgkit.effects.MuteStatusEffect;
import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SoundBarrierEntity;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.google.gson.JsonObject;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MuteEffect extends SpellEffect {
    public final int duration;
    public final boolean muteInside;

    public MuteEffect() {
        this(60*20, true);
    }
    public MuteEffect(int duration, boolean muteInside) {
        this.duration = duration;
        this.muteInside = muteInside;
    }

    @Override
    public void onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        if (!(entity instanceof LivingEntity le)) {
            return;
        }

        var caster = ((ServerWorld)entity.getWorld()).getEntity(cast.getCasterUuid());

        le.addStatusEffect(
                new StatusEffectInstance(ModStatusEffects.MUTE,
                        this.duration, this.muteInside ? MuteStatusEffect.AMPLIFIER_MUTE_INSIDE : MuteStatusEffect.AMPLIFIER_CRUDE,
                        false, false),
                caster
        );
    }

    @Override
    public void onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        // TODO: Attach effect to block so it is locked.
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        var barrier = new SoundBarrierEntity(ModEntities.SOUND_BARRIER, world, box, this.duration, this.muteInside);
        barrier.setCast(cast);
        world.spawnEntity(barrier);
    }

    @Override
    public SpellEffect withParametersFromJSON(JsonObject obj) {
        var duration = this.duration;
        if (obj.has("duration")) {
            duration = obj.get("duration").getAsInt();
        }

        var muteInside = this.muteInside;
        if (obj.has("mute_inside")) {
            muteInside = obj.get("mute_inside").getAsBoolean();
        }

        return new MuteEffect(duration, muteInside);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        obj.addProperty("duration", this.duration);
        obj.addProperty("mute_inside", this.muteInside);
        return obj;
    }

    public static boolean shouldHear(@NotNull LivingEntity hearer, @NotNull Entity source) {
        RPGKitMod.LOGGER.debug("MuteEffect: checking if {} should hear {}", hearer, source);

        var myEffect = hearer.getStatusEffect(ModStatusEffects.MUTE);
        if (myEffect != null && myEffect.getAmplifier() == MuteStatusEffect.AMPLIFIER_CRUDE) {
            return false;
        }
        if (source instanceof LivingEntity li && li.hasStatusEffect(ModStatusEffects.MUTE)) {
            return false;
        }

        return shouldHear(hearer, source.getPos());
    }

    public static boolean shouldHear(@NotNull BlockEntity hearer, @NotNull Entity source) {
        // XXX: Check BlockEntity for populated mute effects.

        RPGKitMod.LOGGER.debug("MuteEffect: checking if {} should hear {}", hearer, source);

        if (source instanceof LivingEntity li && li.hasStatusEffect(ModStatusEffects.MUTE)) {
            return false;
        }

        return shouldHear(hearer, source.getPos());
    }

    public static boolean shouldHear(@NotNull LivingEntity hearer, Vec3d pos) {
        RPGKitMod.LOGGER.debug("MuteEffect: checking if {} should hear sound from {}", hearer, pos);

        var myEffect = hearer.getStatusEffect(ModStatusEffects.MUTE);
        if (myEffect != null && myEffect.getAmplifier() == MuteStatusEffect.AMPLIFIER_CRUDE) {
            return false;
        }

        return shouldHear(hearer, hearer.getPos(), hearer.getWorld(), pos);
    }

    public static boolean shouldHear(@NotNull BlockEntity hearer, Vec3d pos) {
        // XXX: Check BlockEntity for populated mute effects.
        RPGKitMod.LOGGER.debug("MuteEffect: checking if {} should hear sound from {}", hearer, pos);

        var world = hearer.getWorld();
        if (world == null) {
            return true;
        }

        return shouldHear(null, Vec3d.ofCenter(hearer.getPos()), world, pos);
    }

    public static boolean shouldHear(@Nullable Entity except, Vec3d hearerPos, World world, Vec3d pos) {
        RPGKitMod.LOGGER.debug("MuteEffect: checking if sound should pass from {} to {}", pos, hearerPos);

        var barriers = world.getOtherEntities(except, Box.of(pos, 20, 20, 20),
                ent -> ent instanceof SoundBarrierEntity);
        for (var e : barriers) {
            var barrier = (SoundBarrierEntity)e;

            if (barrier.getArea().contains(hearerPos)) {
                if (!barrier.getArea().contains(pos)) {
                    return false;
                }
                if (barrier.shouldMuteInside()) {
                    return false;
                }
            } else if (barrier.getArea().contains(pos)) {
                if (!barrier.getArea().contains(hearerPos)) {
                    return false;
                }
                if (barrier.shouldMuteInside()) {
                    return false;
                }
            }
        }

        return true;
    }
}
