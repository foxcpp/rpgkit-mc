package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.effects.ModStatusEffects;
import com.github.sweetsnowywitch.csmprpgkit.effects.MuteStatusEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.MuteEffect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(at = @At("HEAD"), method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", cancellable = true)
    private void playSound(double x, double y, double z, SoundEvent event, SoundCategory category, float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci) {
        var hearer = Objects.requireNonNull(MinecraftClient.getInstance().player);
        if (!MuteEffect.shouldHear(hearer, new Vec3d(x, y, z))) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "playSoundFromEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFJ)V", cancellable = true)
    private void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        var hearer = Objects.requireNonNull(MinecraftClient.getInstance().player);
        if (!MuteEffect.shouldHear(hearer, entity)) {
            ci.cancel();
        }
    }
}
