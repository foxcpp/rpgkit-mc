package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.perks.ModPerks;
import com.mojang.authlib.GameProfile;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
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
public abstract class AttackDetectionMixin extends PlayerEntity implements PlayerAttackProperties, EntityPlayer_BetterCombat {

    private boolean isModified = false;

    public AttackDetectionMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "attack",
            at = {@At("TAIL")})
    public void attack(Entity target, CallbackInfo ci) {
        RPGKitMod.LOGGER.info("Combo: {}", ModPerks.ON_COMBO.getCombo());
        RPGKitMod.LOGGER.info("Attack Detected, combo: {}", this.getComboCount() % ModPerks.ON_COMBO.getMaxCombo());

        if (ModPerks.ON_COMBO.getCombo() == (this.getComboCount() % ModPerks.ON_COMBO.getMaxCombo())) {
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).
                    addTemporaryModifier(new EntityAttributeModifier(this.getName().getString(), 10,
                            EntityAttributeModifier.Operation.ADDITION));
            isModified = true;
        } else if (isModified) {
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE))
                    .addTemporaryModifier(new EntityAttributeModifier(this.getName().getString(), -10,
                            EntityAttributeModifier.Operation.ADDITION));
            isModified = false;
        }
    }
}
