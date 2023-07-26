package com.github.foxcpp.rpgkitmc.magic.particle;

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
            Codec.INT.fieldOf("color").forGetter(eff -> eff.color),
            Codec.INT.fieldOf("maxAge").forGetter(eff -> eff.color)
    ).apply(instance, GenericSpellParticleEffect::new));

    public static final ParticleEffect.Factory<GenericSpellParticleEffect> FACTORY = new Factory<>() {
        @Override
        public GenericSpellParticleEffect read(ParticleType<GenericSpellParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            var color = reader.readInt();
            var maxAge = reader.readInt();
            return new GenericSpellParticleEffect(color, maxAge);
        }

        @Override
        public GenericSpellParticleEffect read(ParticleType<GenericSpellParticleEffect> type, PacketByteBuf buf) {
            var color = buf.readInt();
            var maxAge = buf.readInt();
            return new GenericSpellParticleEffect(color, maxAge);
        }
    };

    public final int color;
    public final int maxAge;

    public GenericSpellParticleEffect(int color, int maxAge) {
        this.color = color;
        this.maxAge = maxAge;
    }

    @Override
    public ParticleType<GenericSpellParticleEffect> getType() {
        return ModParticles.GENERIC_SPELL;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(color);
        buf.writeInt(maxAge);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %d %d", ModParticles.GENERIC_SPELL, this.color, this.maxAge);
    }
}
