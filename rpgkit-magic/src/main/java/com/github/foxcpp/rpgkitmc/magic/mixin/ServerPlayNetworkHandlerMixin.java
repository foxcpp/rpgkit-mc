package com.github.foxcpp.rpgkitmc.magic.mixin;

import com.github.foxcpp.rpgkitmc.magic.effects.use.special.MuteEffect;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(at = @At("HEAD"), method = "sendPacket(Lnet/minecraft/network/Packet;)V", cancellable = true)
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        var player = ((ServerPlayNetworkHandler) (Object) this).getPlayer();

        if (packet instanceof PlaySoundS2CPacket ps) {
            if (!MuteEffect.shouldHear(player, new Vec3d(ps.getX(), ps.getY(), ps.getZ()))) {
                ci.cancel();
            }
        } else if (packet instanceof PlaySoundFromEntityS2CPacket ps) {
            var entity = player.getWorld().getEntityById(ps.getEntityId());
            if (entity != null && !MuteEffect.shouldHear(player, entity)) {
                ci.cancel();
            }
        }
    }
}
