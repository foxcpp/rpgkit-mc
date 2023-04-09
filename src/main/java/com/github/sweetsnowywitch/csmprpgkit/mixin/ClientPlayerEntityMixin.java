package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.effects.ModStatusEffects;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "playSound(Lnet/minecraft/sound/SoundEvent;FF)V", cancellable = true)
    public void playSound(SoundEvent sound, float volume, float pitch, CallbackInfo ci) {
        var ent = (PlayerEntity)(Object)this;
        if (ent.hasStatusEffect(ModStatusEffects.MUTE)) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "playSound(Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", cancellable = true)
    public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch, CallbackInfo ci) {
        var ent = (PlayerEntity)(Object)this;
        if (ent.hasStatusEffect(ModStatusEffects.MUTE)) {
            ci.cancel();
        }
    }
}
