package com.github.foxcpp.rpgkitmc.magic.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SoundBarrierEntity extends MagicAreaEntity {
    public static final TrackedData<Boolean> MUTE_INSIDE = DataTracker.registerData(SoundBarrierEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public SoundBarrierEntity(EntityType<?> type, World world, Box area, int duration, boolean muteInside) {
        super(type, world, area, duration);
        this.dataTracker.set(MUTE_INSIDE, muteInside);
    }

    public static SoundBarrierEntity empty(EntityType<?> type, World world) {
        return new SoundBarrierEntity(type, world, Box.from(Vec3d.ZERO), 60 * 20, true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(MUTE_INSIDE, true);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(MUTE_INSIDE, nbt.getBoolean("MuteInside"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("MuteInside", this.dataTracker.get(MUTE_INSIDE));
    }

    public boolean shouldMuteInside() {
        return this.dataTracker.get(MUTE_INSIDE);
    }
}
