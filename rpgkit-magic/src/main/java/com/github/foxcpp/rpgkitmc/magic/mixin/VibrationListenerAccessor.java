package com.github.foxcpp.rpgkitmc.magic.mixin;

import net.minecraft.world.event.listener.VibrationListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VibrationListener.class)
public interface VibrationListenerAccessor {
    @Accessor
    VibrationListener.Callback getCallback();
}
