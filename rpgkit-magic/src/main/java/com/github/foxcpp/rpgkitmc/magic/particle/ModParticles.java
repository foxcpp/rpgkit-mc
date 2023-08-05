package com.github.foxcpp.rpgkitmc.magic.particle;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.mojang.serialization.Codec;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModParticles {
    public static final ParticleType<GenericSpellParticleEffect> GENERIC_SPELL = new ParticleType<>(
            false, GenericSpellParticleEffect.FACTORY
    ) {
        @Override
        public Codec<GenericSpellParticleEffect> getCodec() {
            return GenericSpellParticleEffect.CODEC;
        }
    };

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of(RPGKitMagicMod.MOD_ID, "generic_spell"), GENERIC_SPELL);
    }
}
