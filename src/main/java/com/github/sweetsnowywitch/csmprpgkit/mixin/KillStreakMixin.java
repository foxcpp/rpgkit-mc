package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.perks.ModPerks;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class KillStreakMixin extends PlayerEntity {

    public KillStreakMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "attack",
            at = {@At("TAIL")})
    public void attack(Entity target, CallbackInfo ci) {
        if (ModPerks.KILL_STREAK.getKillStreak() < ModPerks.KILL_STREAK.getMaxStreak() && !target.isAlive()) {
            Objects.requireNonNull(this.getAttributeInstance(ModPerks.KILL_STREAK.getAttribute())).
                    addTemporaryModifier(new EntityAttributeModifier(this.getName().getString(),
                            ModPerks.KILL_STREAK.getModifier(),
                            EntityAttributeModifier.Operation.ADDITION));
            ModPerks.KILL_STREAK.setKillStreak(ModPerks.KILL_STREAK.getKillStreak() + 1);
        }
    }

    @Inject(method = "onDeath",
            at = {@At("TAIL")})
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        if (ModPerks.KILL_STREAK.getKillStreak() > 0) {
            ModPerks.KILL_STREAK.setKillStreak(Math.max(0, ModPerks.KILL_STREAK.getKillStreak() - ModPerks.KILL_STREAK.getPenalty()));
        }
    }

    @Inject(method = "onSpawn",
            at = {@At("TAIL")})
    public void onSpawn(CallbackInfo ci) {
        RPGKitMod.LOGGER.info("Current streak: {}", ModPerks.KILL_STREAK.getKillStreak());
        Objects.requireNonNull(this.getAttributeInstance(ModPerks.KILL_STREAK.getAttribute())).
                addTemporaryModifier(new EntityAttributeModifier(this.getName().getString(),
                        ModPerks.KILL_STREAK.getKillStreak() * ModPerks.KILL_STREAK.getModifier(),
                        EntityAttributeModifier.Operation.ADDITION));
    }
}