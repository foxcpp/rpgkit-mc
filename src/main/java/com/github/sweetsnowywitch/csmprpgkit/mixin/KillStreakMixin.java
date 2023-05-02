package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.perks.KillStreakPerk;
import com.github.sweetsnowywitch.csmprpgkit.classes.perks.ModPerks;
import com.github.sweetsnowywitch.csmprpgkit.classes.perks.SingleKillPerk;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
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
        var perks = ModComponents.CLASS.get(this).getPerk(ModPerks.KILL_STREAK.typeId);

        for (var rawPerk : perks) {
            var perk = (KillStreakPerk) rawPerk;
            if (perk.getAttribute() != null && perk.getKillStreak() < perk.getMaxStreak() && !target.isAlive()) {
                Objects.requireNonNull(this.getAttributeInstance(perk.getAttribute())).
                        addTemporaryModifier(new EntityAttributeModifier(this.getName().getString(), perk.getModifier(),
                                EntityAttributeModifier.Operation.ADDITION));
                perk.setKillStreak(perk.getKillStreak() + 1);
            }
        }
    }

    @Inject(method = "onDeath",
            at = {@At("TAIL")})
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        var perks = ModComponents.CLASS.get(this).getPerk(ModPerks.KILL_STREAK.typeId);

        for (var rawPerk : perks) {
            var perk = (KillStreakPerk) rawPerk;
            if (perk.getAttribute() != null && perk.getKillStreak() > 0) {
                perk.setKillStreak(Math.max(0, perk.getKillStreak() - perk.getPenalty()));
            }
        }
    }

    @Inject(method = "onSpawn",
            at = {@At("TAIL")})
    public void onSpawn(CallbackInfo ci) {
        var perks = ModComponents.CLASS.get(this).getPerk(ModPerks.KILL_STREAK.typeId);

        for (var rawPerk : perks) {
            var perk = (KillStreakPerk) rawPerk;
            if (perk.getAttribute() != null) {
                Objects.requireNonNull(this.getAttributeInstance(perk.getAttribute())).
                        addTemporaryModifier(new EntityAttributeModifier(this.getName().getString(),
                                perk.getKillStreak() * perk.getModifier(),
                                EntityAttributeModifier.Operation.ADDITION));
            }
        }
    }
}