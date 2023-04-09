package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.effects.ModStatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("HEAD"), method = "isSilent()Z", cancellable = true)
    public void isSilent(CallbackInfoReturnable<Boolean> cir) {
        var ent = ((Entity)(Object)this);
        if (ent instanceof LivingEntity le) {
            if (le.hasStatusEffect(ModStatusEffects.MUTE)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "occludeVibrationSignals()Z", cancellable = true)
    public void occludeVibrationSignals(CallbackInfoReturnable<Boolean> cir) {
        var ent = ((Entity)(Object)this);
        if (ent instanceof LivingEntity le) {
            if (le.hasStatusEffect(ModStatusEffects.MUTE)) {
                cir.setReturnValue(true);
            }
        }
    }
}
