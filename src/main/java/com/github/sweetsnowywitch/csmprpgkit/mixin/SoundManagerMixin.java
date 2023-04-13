package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.magic.effects.MuteEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(at = @At("HEAD"), method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", cancellable = true)
    public void play(SoundInstance sound, CallbackInfo ci) {
        var hearer = MinecraftClient.getInstance().player;
        if (hearer == null) {
            return;
        }

        if (!MuteEffect.shouldHear(hearer, new Vec3d(sound.getX(), sound.getY(), sound.getZ()))) {
            ci.cancel();
        }
    }
}
