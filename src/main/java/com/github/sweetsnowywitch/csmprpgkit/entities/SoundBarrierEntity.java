package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.particle.GenericSpellParticleEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class SoundBarrierEntity extends MagicAreaEntity {
    public static final TrackedData<Boolean> MUTE_INSIDE = DataTracker.registerData(SoundBarrierEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public SoundBarrierEntity(EntityType<?> type, World world, Box area, int duration, boolean muteInside) {
        super(type, world, area, duration);
        this.dataTracker.set(MUTE_INSIDE, muteInside);
    }

    public static SoundBarrierEntity empty(EntityType<?> type, World world) {
        return new SoundBarrierEntity(type, world, Box.from(Vec3d.ZERO), 60*20, true);
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

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
    public boolean shouldMuteInside() {
        return this.dataTracker.get(MUTE_INSIDE);
    }
}
