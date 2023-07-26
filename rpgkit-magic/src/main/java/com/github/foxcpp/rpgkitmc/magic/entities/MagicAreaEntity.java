package com.github.foxcpp.rpgkitmc.magic.entities;

import com.github.foxcpp.rpgkitmc.TrackedHandlers;
import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.particle.GenericSpellParticleEffect;
import com.github.foxcpp.rpgkitmc.magic.EffectVector;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellElement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class MagicAreaEntity extends Entity {
    // Populated only on logical server side.
    public ServerSpellCast cast = null;
    protected int maxAge;
    public static final TrackedData<Box> AREA = DataTracker.registerData(MagicAreaEntity.class, TrackedHandlers.BOX);
    public static final TrackedData<SpellCast> CAST = DataTracker.registerData(MagicAreaEntity.class, SpellCast.TRACKED_HANDLER);
    public static final TrackedData<Boolean> NO_PARTICLES = DataTracker.registerData(MagicAreaEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Optional<EffectVector>> PARTICLE_VECTOR = DataTracker.registerData(MagicAreaEntity.class, EffectVector.OPTIONAL_TRACKED_HANDLER);
    protected int particleColor;
    protected ParticleEffect particleEffect;

    public MagicAreaEntity(EntityType<?> type, World world, Box area, int duration) {
        super(type, world);
        this.setArea(area);
        this.maxAge = duration;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(AREA, Box.from(this.getPos()));
        this.dataTracker.startTracking(CAST, SpellCast.EMPTY);
        this.dataTracker.startTracking(NO_PARTICLES, false);
        this.dataTracker.startTracking(PARTICLE_VECTOR, Optional.empty());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(AREA, TrackedHandlers.BOX_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("Area"))
                .resultOrPartial(RPGKitMagicMod.LOGGER::error).orElse(Box.from(this.getPos())));

        if (nbt.contains("Cast")) {
            this.cast = ServerSpellCast.readFromNbt(nbt.getCompound("Cast")); // full data for server
            this.dataTracker.set(CAST, this.cast); // sync some spell data to client
        }
        this.maxAge = nbt.getInt("MaxAge");
        this.dataTracker.set(NO_PARTICLES, nbt.getBoolean("NoParticles"));
        if (nbt.contains("ParticleVector")) {
            this.dataTracker.set(PARTICLE_VECTOR, Optional.of(EffectVector.valueOf(nbt.getString("ParticleVector"))));
        } else {
            this.dataTracker.set(PARTICLE_VECTOR, Optional.empty());
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        var castNBT = new NbtCompound();
        if (this.cast != null) {
            // Server with full data.
            this.cast.writeToNbt(castNBT);
        } else {
            // Client with partial data.
            this.dataTracker.get(CAST).writeToNbt(castNBT);
        }
        nbt.put("Cast", castNBT);

        nbt.put("Area",
                TrackedHandlers.BOX_CODEC.encodeStart(NbtOps.INSTANCE, this.dataTracker.get(AREA))
                        .resultOrPartial(RPGKitMagicMod.LOGGER::error).orElseThrow());

        nbt.putInt("MaxAge", this.maxAge);
        nbt.putBoolean("NoParticles", this.dataTracker.get(NO_PARTICLES));

        var particleVector = this.dataTracker.get(PARTICLE_VECTOR);
        particleVector.ifPresent(effectVector -> nbt.putString("ParticleVector", effectVector.name()));
    }

    public void increaseMaxAge(int duration) {
        this.maxAge += duration;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (CAST.equals(data)) {
            this.particleColor = SpellElement.calculateBaseColor(this.dataTracker.get(CAST).getFullRecipe());
            this.particleEffect = new GenericSpellParticleEffect(this.particleColor, 10);
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.changing((float) this.dataTracker.get(AREA).getXLength(), (float) this.dataTracker.get(AREA).getYLength());
    }

    public void setCast(@NotNull ServerSpellCast cast) {
        this.cast = cast;
        this.dataTracker.set(CAST, cast);
    }

    public SpellCast getCast() {
        if (this.cast != null) {
            return this.cast;
        }
        return this.dataTracker.get(CAST);
    }

    protected void setArea(Box area) {
        this.dataTracker.set(AREA, area);
        this.setPosition(area.getCenter());
    }

    public Box getArea() {
        return this.dataTracker.get(AREA);
    }

    public void setParticleVector(EffectVector vec) {
        this.dataTracker.set(PARTICLE_VECTOR, Optional.of(vec));
    }

    protected void spawnParticles() {
        if (this.particleEffect == null) {
            // might not be initialized if server sync lags
            return;
        }

        var area = this.getArea();
        var volume = (area.maxX - area.minX) * (area.maxZ - area.minZ) * (area.maxY - area.minY);

        var vector = this.dataTracker.get(PARTICLE_VECTOR);

        for (int i = 0; i < volume / 160 || i == 0; i++) {
            if (vector.isPresent()) {
                var x = RPGKitMagicMod.RANDOM.nextDouble(area.minX, area.maxX);
                var y = RPGKitMagicMod.RANDOM.nextDouble(area.minY, area.maxY);
                var z = RPGKitMagicMod.RANDOM.nextDouble(area.minZ, area.maxZ);
                var direction = vector.get().direction(new Vec3d(x, y, z), this.getPos(), Vec3d.ZERO);
                this.world.addParticle(this.particleEffect, x, y, z,
                        direction.x, direction.y, direction.z);
            } else {
                this.world.addParticle(this.particleEffect,
                        RPGKitMagicMod.RANDOM.nextDouble(area.minX, area.maxX),
                        RPGKitMagicMod.RANDOM.nextDouble(area.minY, area.maxY),
                        RPGKitMagicMod.RANDOM.nextDouble(area.minZ, area.maxZ),
                        0, 0, 0);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClient && this.age > maxAge) {
            this.discard();
            return;
        }

        if (this.world.isClient && this.getArea() != null && !this.dataTracker.get(NO_PARTICLES)) {
            this.spawnParticles();
        }
    }
}
