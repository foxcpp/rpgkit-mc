package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.magic.effects.WardEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow public abstract List<BlockPos> getAffectedBlocks();

    @Inject(at = @At("TAIL"), method = "collectBlocksAndDamageEntities()V")
    public void collectBlocksAndDamageEntities(CallbackInfo ci) {
        var me = (Explosion)(Object)this;
        var world = ((ExplosionAccessor)me).getWorld();
        if (!(world instanceof ServerWorld sw)) {
            return;
        }

        this.getAffectedBlocks().removeIf(pos -> WardEffect.isBlockProtected(sw, pos));
    }
}
