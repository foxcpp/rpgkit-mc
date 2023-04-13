package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.magic.effects.MuteEffect;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.VibrationListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationListener.class)
public class VibrationListenerMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/event/listener/VibrationListener$Callback;accepts(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/listener/GameEventListener;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;)Z"),
            method = "listen(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/GameEvent$Message;)Z",
            cancellable = true)
    public void listen(ServerWorld world, GameEvent.Message event, CallbackInfoReturnable<Boolean> cir) {
        var me = (VibrationListener)(Object)this;
        var cb = ((VibrationListenerAccessor)me).getCallback();

        if (cb instanceof LivingEntity le) {
            if (event.getEmitter().sourceEntity() != null && !MuteEffect.shouldHear(le, event.getEmitter().sourceEntity())) {
                cir.setReturnValue(false);
            } else if (!MuteEffect.shouldHear(le, event.getEmitterPos())) {
                cir.setReturnValue(false);
            }
        } else if (cb instanceof BlockEntity be) {
            if (event.getEmitter().sourceEntity() != null && !MuteEffect.shouldHear(be, event.getEmitter().sourceEntity())) {
                cir.setReturnValue(false);
            } else if (!MuteEffect.shouldHear(be, event.getEmitterPos())) {
                cir.setReturnValue(false);
            }
        }
        // TODO: Handle AllayEntity.VibrationListenerCallback.
    }
}
