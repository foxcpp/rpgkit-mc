package com.github.foxcpp.rpgkitmc.magic.mixin;

import com.github.foxcpp.rpgkitmc.magic.effects.use.special.MuteEffect;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Vibrations.VibrationListener.class)
public class VibrationListenerMixin {
    @Inject(at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/event/Vibrations$Callback;accepts(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;)Z"
            ),
            method = "listen(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;Lnet/minecraft/util/math/Vec3d;)Z",
            cancellable = true)
    public void listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        var me = (Vibrations.VibrationListener) (Object) this;
        var recv = ((VibrationListenerAccessor) me).getReceiver();
        var cb = recv.getVibrationCallback();

        // XXX: This is broken since 1.20 - Callback is no longer directly implemented by BlockEntity or LivingEntity.

        if (cb instanceof LivingEntity le) {
            if (emitter.sourceEntity() != null && !MuteEffect.shouldHear(le, emitter.sourceEntity())) {
                cir.setReturnValue(false);
            } else if (!MuteEffect.shouldHear(le, emitterPos)) {
                cir.setReturnValue(false);
            }
        } else if (cb instanceof BlockEntity be) {
            if (emitter.sourceEntity() != null && !MuteEffect.shouldHear(be, emitter.sourceEntity())) {
                cir.setReturnValue(false);
            } else if (!MuteEffect.shouldHear(be, emitterPos)) {
                cir.setReturnValue(false);
            }
        }
        // TODO: Handle AllayEntity.VibrationListenerCallback.
    }
}
