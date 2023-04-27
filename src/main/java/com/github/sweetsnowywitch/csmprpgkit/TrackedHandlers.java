package com.github.sweetsnowywitch.csmprpgkit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class TrackedHandlers {
    public static final Codec<BlockBox> BLOCKBOX_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("minX").forGetter(BlockBox::getMinX),
            Codec.INT.fieldOf("minY").forGetter(BlockBox::getMinY),
            Codec.INT.fieldOf("minZ").forGetter(BlockBox::getMinZ),
            Codec.INT.fieldOf("maxX").forGetter(BlockBox::getMaxX),
            Codec.INT.fieldOf("maxY").forGetter(BlockBox::getMaxY),
            Codec.INT.fieldOf("maxZ").forGetter(BlockBox::getMaxZ)
    ).apply(instance, BlockBox::new));

    public static final Codec<Box> BOX_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("minX").forGetter(b -> b.minX),
            Codec.DOUBLE.fieldOf("minY").forGetter(b -> b.minY),
            Codec.DOUBLE.fieldOf("minZ").forGetter(b -> b.minZ),
            Codec.DOUBLE.fieldOf("maxX").forGetter(b -> b.maxX),
            Codec.DOUBLE.fieldOf("maxY").forGetter(b -> b.maxY),
            Codec.DOUBLE.fieldOf("maxZ").forGetter(b -> b.maxZ)
    ).apply(instance, Box::new));
    public static final TrackedDataHandler<Box> BOX = new TrackedDataHandler.ImmutableHandler<>() {
        @Override
        public void write(PacketByteBuf buf, Box value) {
            buf.writeNbt((NbtCompound) BOX_CODEC.encodeStart(NbtOps.INSTANCE, value)
                    .resultOrPartial(RPGKitMod.LOGGER::error).orElseThrow());
        }

        @Override
        public Box read(PacketByteBuf buf) {
            return BOX_CODEC.parse(NbtOps.INSTANCE, buf.readNbt())
                    .resultOrPartial(RPGKitMod.LOGGER::error).orElseThrow();
        }
    };
    public static final TrackedDataHandler<Vec3d> VEC3D = new TrackedDataHandler.ImmutableHandler<>() {
        @Override
        public void write(PacketByteBuf buf, Vec3d value) {
            buf.writeDouble(value.getX());
            buf.writeDouble(value.getY());
            buf.writeDouble(value.getZ());
        }

        @Override
        public Vec3d read(PacketByteBuf buf) {
            return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    };

    public static final TrackedDataHandler<Optional<Vec3d>> OPTIONAL_VEC3D = TrackedDataHandler.ofOptional(VEC3D::write, VEC3D::read);

    public static void register() {
        TrackedDataHandlerRegistry.register(BOX);
        TrackedDataHandlerRegistry.register(VEC3D);
        TrackedDataHandlerRegistry.register(OPTIONAL_VEC3D);
    }
}
