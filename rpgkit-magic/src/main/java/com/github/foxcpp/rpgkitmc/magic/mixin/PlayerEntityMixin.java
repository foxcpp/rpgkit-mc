package com.github.foxcpp.rpgkitmc.magic.mixin;

import com.github.foxcpp.rpgkitmc.magic.statuseffects.ModStatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "playSound(Lnet/minecraft/sound/SoundEvent;FF)V", cancellable = true)
    public void playSound(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        var ent = (PlayerEntity) (Object) this;
        if (ent.hasStatusEffect(ModStatusEffects.MUTE)) {
            ci.cancel();
        }
    }
}
