package com.github.foxcpp.rpgkitmc.magic.mixin;

import com.github.foxcpp.rpgkitmc.magic.statuseffects.SealedStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements SealedStatusEffect.Sealable {
    private boolean sealed = false;

    @Override
    public void setSealed(boolean v) {
        this.sealed = v;
    }

    @Inject(at = @At("HEAD"), method = "getNextAirOnLand(I)I", cancellable = true)
    public void getNextAirOnLand(int air, CallbackInfoReturnable<Integer> cir) {
        if (this.sealed) {
            cir.setReturnValue(Math.max(air - 1, 0));
        }
    }

    @Inject(at = @At("HEAD"), method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", cancellable = true)
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (source.isOf(DamageTypes.OUT_OF_WORLD) || source.isOf(DamageTypes.MAGIC)) {
            return;
        }
        if ((source.isIn(DamageTypeTags.BYPASSES_ARMOR) || source.isIn(DamageTypeTags.BYPASSES_EFFECTS)) &&
                !source.isOf(DamageTypes.LAVA) && !source.isOf(DamageTypes.HOT_FLOOR) &&
                !source.isOf(DamageTypes.IN_WALL) && !source.isOf(DamageTypes.STALAGMITE)) {
            return;
        }
        if (this.sealed) {
            ci.cancel();
        }
    }
}
