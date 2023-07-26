package com.github.foxcpp.rpgkitmc.magic.entities;

import com.github.foxcpp.rpgkitmc.magic.effects.AreaEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PersistentMagicEntity extends MagicAreaEntity {
    private int effectInterval;

    public PersistentMagicEntity(EntityType<?> type, World world, Box area, int duration, int effectInterval) {
        super(type, world, area, duration);
        this.effectInterval = effectInterval;
    }

    public static PersistentMagicEntity empty(EntityType<?> type, World world) {
        return new PersistentMagicEntity(type, world, Box.from(Vec3d.ZERO), 50, 50);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putInt("EffectInterval", this.effectInterval);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("EffectInterval")) {
            this.effectInterval = nbt.getInt("EffectInterval");
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    public void tick() {
        super.tick();

        if (!this.world.isClient && (this.effectInterval == 0 || this.age % this.effectInterval == 0)) {
            this.cast.getSpell().useOnArea(this.cast, (ServerWorld) world,
                    this.getArea(), this.getPos(), AreaEffect.AreaCollider.cube(this.getArea()));
        }
    }
}
