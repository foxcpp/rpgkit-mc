package com.github.foxcpp.rpgkitmc.mixin;

import com.github.foxcpp.rpgkitmc.classes.perks.ModPerks;
import com.github.foxcpp.rpgkitmc.classes.perks.SingleKillPerk;
import com.github.foxcpp.rpgkitmc.components.ModComponents;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
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

@Mixin(ServerPlayerEntity.class)
public abstract class SingleKillMixin extends PlayerEntity {
    public SingleKillMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "attack",
            at = {@At("TAIL")})
    public void attack(Entity target, CallbackInfo ci) {
        var perks = ModComponents.CLASS.get(this).getPerk(ModPerks.SINGLE_KILL.typeId);

        for (var rawPerk : perks) {
            var perk = (SingleKillPerk) rawPerk;
            if (perk.getStatusEffect() != null && !target.isAlive() && (!perk.isOnlyPlayerKill() || target.isPlayer())) {
                this.addStatusEffect(new StatusEffectInstance(perk.getStatusEffect(), perk.getDuration(),
                        perk.getAmplifier(), false, false), this);
            }
        }
    }
}
