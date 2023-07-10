package com.github.sweetsnowywitch.rpgkit.magic.entities;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;

public class ForcefieldEntity extends MagicAreaEntity {
    private final HashMap<Integer, Boolean> prevTickSeen = new HashMap<>(5);
    private Box innerArea;
    private static final double THICKNESS = 1.1;

    public ForcefieldEntity(EntityType<?> type, World world, Box area, int duration) {
        super(type, world, area, duration);
    }

    public static ForcefieldEntity empty(EntityType<?> type, World world) {
        return new ForcefieldEntity(type, world, Box.from(Vec3d.ZERO), 50);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected void setArea(Box area) {
        super.setArea(area);
        this.innerArea = area.contract(THICKNESS, THICKNESS, THICKNESS);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (AREA.equals(data)) {
            this.innerArea = this.getArea().contract(THICKNESS, THICKNESS, THICKNESS);
        }
    }

    @Override
    protected void spawnParticles() {
        var area = this.getArea();

        var floorArea = (area.maxX - area.minX) * (area.maxZ - area.minZ);
        for (int i = 0; i < floorArea / 160 || i == 0; i++) {
            this.world.addParticle(this.particleEffect,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minX, area.maxX),
                    area.minY,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minZ, area.maxZ),
                    0, 0, 0);
            this.world.addParticle(this.particleEffect,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minX, area.maxX),
                    area.maxY,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minZ, area.maxZ),
                    0, 0, 0);
        }
        var xyArea = (area.maxX - area.minX) * (area.maxY - area.minY);
        for (int i = 0; i < xyArea / 160 || i == 0; i++) {
            this.world.addParticle(this.particleEffect,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minX, area.maxX),
                    RPGKitMagicMod.RANDOM.nextDouble(area.minY, area.maxY),
                    area.minZ,
                    0, 0, 0);
            this.world.addParticle(this.particleEffect,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minX, area.maxX),
                    RPGKitMagicMod.RANDOM.nextDouble(area.minY, area.maxY),
                    area.maxZ,
                    0, 0, 0);
        }
        var zyArea = (area.maxZ - area.minZ) * (area.maxY - area.minY);
        for (int i = 0; i < zyArea / 160 || i == 0; i++) {
            this.world.addParticle(this.particleEffect,
                    area.minX,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minY, area.maxY),
                    RPGKitMagicMod.RANDOM.nextDouble(area.minZ, area.maxZ),
                    0, 0, 0);
            this.world.addParticle(this.particleEffect,
                    area.maxX,
                    RPGKitMagicMod.RANDOM.nextDouble(area.minY, area.maxY),
                    RPGKitMagicMod.RANDOM.nextDouble(area.minZ, area.maxZ),
                    0, 0, 0);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient) {
            return;
        }
        var world = (ServerWorld) this.world;

        var area = this.getArea();

        for (var ent : this.prevTickSeen.entrySet()) {
            ent.setValue(false);
        }

        for (var ent : world.getOtherEntities(this, area)) {
            var pos = ent.getPos();

            if (!area.contains(pos) || this.innerArea.contains(pos)) {
                continue;
            }

            if (cast.getCasterUuid().equals(ent.getUuid())) {
                continue;
            }
            if (!this.prevTickSeen.containsKey(ent.getId())) {
                this.cast.getSpell().useOnEntity(this.cast, ent);
                world.spawnParticles(this.particleEffect, ent.getX(), ent.getEyeY(), ent.getZ(),
                        5, 0.2, 0.7, 0.2, 0);
            }
            this.prevTickSeen.put(ent.getId(), true);
        }

        this.prevTickSeen.entrySet().removeIf(v -> !v.getValue());
    }
}
