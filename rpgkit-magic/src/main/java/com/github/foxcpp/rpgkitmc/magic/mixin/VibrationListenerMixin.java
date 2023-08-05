package com.github.foxcpp.rpgkitmc.magic.mixin;

import com.github.foxcpp.rpgkitmc.magic.effects.use.special.MuteEffect;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.VibrationListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationListener.class)
public class VibrationListenerMixin {
    @Inject(at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/event/listener/VibrationListener$Callback;accepts(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/listener/GameEventListener;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;)Z"
            ),
            method = "listen(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/world/event/GameEvent$Emitter;Lnet/minecraft/util/math/Vec3d;)Z",
            cancellable = true)
    public void listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos, CallbackInfoReturnable<Boolean> cir) {
        var me = (VibrationListener) (Object) this;
        var cb = ((VibrationListenerAccessor) me).getCallback();

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
