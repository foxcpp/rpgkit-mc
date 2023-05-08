package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.TrackedHandlers;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Optional;

public class SpellBlastEntity extends MagicAreaEntity {
    private final HashMap<Integer, Boolean> prevTickSeen = new HashMap<>(5);
    public static final TrackedData<Optional<Vec3d>> PARTICLE_ORIGIN = DataTracker.registerData(SpellBlastEntity.class, TrackedHandlers.OPTIONAL_VEC3D);
    public static final TrackedData<Integer> PARTICLE_SPREAD_AGE = DataTracker.registerData(SpellBlastEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public SpellBlastEntity(EntityType<?> type, World world, Vec3d origin, double distance, double radius, Vec3d directionVec, int duration) {
        super(type, world, Box.from(Vec3d.ZERO), duration);
        this.dataTracker.set(MagicAreaEntity.AREA, this.computeArea(origin, directionVec, distance, radius));
        this.dataTracker.set(PARTICLE_ORIGIN, Optional.of(origin));
        this.dataTracker.set(PARTICLE_SPREAD_AGE, 5);
        this.setBoundingBox(this.getArea());
        this.setPosition(origin);
    }

    public static SpellBlastEntity empty(EntityType<?> type, World world) {
        return new SpellBlastEntity(type, world, Vec3d.ZERO, 1, 1, new Vec3d(0, 1, 0), 3 * 20);
    }

    public void moveArea(Vec3d origin, Vec3d direction, double distance, double radius) {
        this.dataTracker.set(MagicAreaEntity.AREA, this.computeArea(origin, direction, distance, radius));
        this.dataTracker.set(PARTICLE_ORIGIN, Optional.of(origin));
        this.dataTracker.set(PARTICLE_SPREAD_AGE, this.age + 5);
        this.setBoundingBox(this.getArea());
        this.setPosition(origin);
    }

    private Box computeArea(Vec3d origin, Vec3d direction, double distance, double radius) {
        direction = direction.multiply(distance / direction.length());
        var end = origin.add(direction);
        BlockHitResult hitResult = this.world.raycast(new RaycastContext(
                origin, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                this));
        if (hitResult.getType() != HitResult.Type.MISS) {
            var unit = hitResult.getSide().getUnitVector();
            end = hitResult.getPos().add(-unit.getX(), -unit.getY(), -unit.getZ());
        }

        var line = new Box(origin, end);

        double dx = 0, dy = 0, dz = 0;
        if (line.getXLength() < radius) dx = line.getXLength() - radius;
        if (line.getYLength() < radius) dy = line.getYLength() - radius;
        if (line.getZLength() < radius) dz = line.getZLength() - radius;

        return line.expand(dx, dy, dz);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PARTICLE_ORIGIN, Optional.empty());
        this.dataTracker.startTracking(PARTICLE_SPREAD_AGE, 10);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        var particleOrigin = this.dataTracker.get(PARTICLE_ORIGIN);
        if (particleOrigin.isPresent()) {
            var origin = particleOrigin.get();
            var pos = new NbtCompound();
            pos.putDouble("X", origin.getX());
            pos.putDouble("Y", origin.getY());
            pos.putDouble("Z", origin.getZ());
            nbt.put("ParticleOrigin", pos);
        }

        var particleSpreadAge = this.dataTracker.get(PARTICLE_SPREAD_AGE);
        if (particleSpreadAge > 0) {
            nbt.putInt("ParticleSpreadAge", particleSpreadAge);
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ParticleOrigin")) {
            var pos = nbt.getCompound("ParticleOrigin");
            this.dataTracker.set(PARTICLE_ORIGIN, Optional.of(new Vec3d(
                    pos.getDouble("X"), pos.getDouble("Y"), pos.getDouble("Z")
            )));
        }
        if (nbt.contains("ParticleSpreadAge")) {
            this.dataTracker.set(PARTICLE_SPREAD_AGE, nbt.getInt("ParticleSpreadAge"));
        }
    }


    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        var box = this.getBoundingBox();
        return EntityDimensions.changing((float) box.getXLength(), (float) box.getYLength());
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    protected void spawnParticles() {
        var origin = this.dataTracker.get(PARTICLE_ORIGIN);

        if (origin.isPresent()) {
            var area = this.getArea();
            var volume = (area.maxX - area.minX) * (area.maxZ - area.minZ) * (area.maxY - area.minY);

            var pos = origin.get();
            var x = RPGKitMod.RANDOM.nextDouble(area.minX, area.maxX);
            var y = RPGKitMod.RANDOM.nextDouble(area.minY, area.maxY);
            var z = RPGKitMod.RANDOM.nextDouble(area.minZ, area.maxZ);
            var velocity = new Vec3d(x - pos.getX(), y - pos.getY(), z - pos.getZ());
            velocity = velocity.multiply((velocity.length() / this.maxAge * 5f) / velocity.length());
            for (int i = 0; i < volume / 80 || i == 0; i++) {
                this.world.addParticle(this.particleEffect,
                        pos.getX(), pos.getY(), pos.getZ(),
                        velocity.x, velocity.y, velocity.z);
            }
        } else {
            super.spawnParticles();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient) {
            return;
        }

        var cast = (ServerSpellCast) this.getCast();

        for (var ent : this.prevTickSeen.entrySet()) {
            ent.setValue(false);
        }

        if (this.age <= 5) {
            return;
        }

        for (var ent : this.world.getOtherEntities(this, this.getArea())) {
            if (cast.getCasterUuid().equals(ent.getUuid())) {
                continue;
            }
            if (!this.prevTickSeen.containsKey(ent.getId())) {
                this.cast.getSpell().onSingleEntityHit(this.cast, ent);
            }
            this.prevTickSeen.put(ent.getId(), true);
        }

        this.prevTickSeen.entrySet().removeIf(v -> !v.getValue());
    }
}
