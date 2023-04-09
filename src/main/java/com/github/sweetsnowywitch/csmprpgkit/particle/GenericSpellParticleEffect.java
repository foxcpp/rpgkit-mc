package com.github.sweetsnowywitch.csmprpgkit.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

import java.util.Locale;

public class GenericSpellParticleEffect implements ParticleEffect {
    public static final Codec<GenericSpellParticleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("color").forGetter(eff -> eff.color)
    ).apply(instance, GenericSpellParticleEffect::new));

    public static final ParticleEffect.Factory<GenericSpellParticleEffect> FACTORY = new Factory<>() {
        @Override
        public GenericSpellParticleEffect read(ParticleType<GenericSpellParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            var color = reader.readInt();
            return new GenericSpellParticleEffect(color);
        }

        @Override
        public GenericSpellParticleEffect read(ParticleType<GenericSpellParticleEffect> type, PacketByteBuf buf) {
            var color = buf.readInt();
            return new GenericSpellParticleEffect(color);
        }
    };

    public final int color;

    public GenericSpellParticleEffect(int color) {
        this.color = color;
    }

    @Override
    public ParticleType<GenericSpellParticleEffect> getType() {
        return ModParticles.GENERIC_SPELL;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(color);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %d", ModParticles.GENERIC_SPELL, this.color);
    }
}
