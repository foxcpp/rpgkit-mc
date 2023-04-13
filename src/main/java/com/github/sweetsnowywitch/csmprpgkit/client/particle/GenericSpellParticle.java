package com.github.sweetsnowywitch.csmprpgkit.client.particle;

import com.github.sweetsnowywitch.csmprpgkit.particle.GenericSpellParticleEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GenericSpellParticle extends AscendingParticle {
    GenericSpellParticle(ClientWorld world, double x, double y, double z, int color, SpriteProvider spriteProvider) {
        super(world, x, y, z,
                0.1f, -0.01f, 0.1f,
                0f, 0f, 0f,
                1.0f, spriteProvider, 0.5f, 20,
                0.01f, true);

        this.red = (float)ColorHelper.Argb.getRed(color) / 255f;
        this.green = (float)ColorHelper.Argb.getGreen(color) / 255f;
        this.blue = (float)ColorHelper.Argb.getBlue(color) / 255f;
        this.alpha = 0.2f;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory
    implements ParticleFactory<GenericSpellParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(GenericSpellParticleEffect parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new  GenericSpellParticle(world, x, y, z, parameters.color, spriteProvider);
        }
    }
}
