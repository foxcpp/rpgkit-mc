package com.github.foxcpp.rpgkitmc.magic.mixin;

import net.minecraft.world.event.Vibrations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vibrations.VibrationListener.class)
public interface VibrationListenerAccessor {
    @Accessor
    Vibrations getReceiver();
}
