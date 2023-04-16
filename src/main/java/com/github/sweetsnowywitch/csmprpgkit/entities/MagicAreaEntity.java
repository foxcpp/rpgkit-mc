package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import com.github.sweetsnowywitch.csmprpgkit.particle.GenericSpellParticleEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class MagicAreaEntity extends Entity {
    // Populated only on logical server side.
    public ServerSpellCast cast = null;
    protected int maxAge = 15*20;
    public static final TrackedData<Box> AREA = DataTracker.registerData(MagicAreaEntity.class, RPGKitMod.BOX_TRACKED_HANDLER);
    public static final TrackedData<SpellCast> CAST = DataTracker.registerData(MagicAreaEntity.class, SpellCast.TRACKED_HANDLER);
    protected int particleColor;

    public MagicAreaEntity(EntityType<?> type, World world, Box area, int duration) {
        super(type, world);
        this.dataTracker.set(AREA, area);
        this.maxAge = duration;
        this.setPosition(area.getCenter());
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(AREA, Box.from(this.getPos()));
        this.dataTracker.startTracking(CAST, SpellCast.EMPTY);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(AREA, RPGKitMod.BOX_CODEC.parse(NbtOps.INSTANCE, nbt.getCompound("Area"))
                .resultOrPartial(RPGKitMod.LOGGER::error).orElse(Box.from(this.getPos())));

        if (nbt.contains("Cast")) {
            this.cast = ServerSpellCast.readFromNbt(nbt.getCompound("Cast")); // full data for server
            this.dataTracker.set(CAST, this.cast); // sync some spell data to client
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
                RPGKitMod.BOX_CODEC.encodeStart(NbtOps.INSTANCE, this.dataTracker.get(AREA))
                        .resultOrPartial(RPGKitMod.LOGGER::error).orElseThrow());
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (CAST.equals(data)) {
            this.particleColor = SpellElement.calculateBaseColor(this.dataTracker.get(CAST).getFullRecipe());
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.changing((float)this.dataTracker.get(AREA).getXLength(), (float)this.dataTracker.get(AREA).getYLength());
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

    public Box getArea() {
        return this.dataTracker.get(AREA);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClient && this.age > maxAge) {
            this.discard();
            return;
        }

        if (this.world.isClient && this.getArea() != null) {
            var area = this.getArea();
            var volume = (area.maxX - area.minX) * (area.maxZ - area.minZ) * (area.maxY - area.minY);
            for (int i = 0; i < volume / 160 || i == 0; i++) {
                this.world.addParticle(new GenericSpellParticleEffect(this.particleColor),
                        RPGKitMod.RANDOM.nextDouble(area.minX, area.maxX),
                        RPGKitMod.RANDOM.nextDouble(area.minY, area.maxY),
                        RPGKitMod.RANDOM.nextDouble(area.minZ, area.maxZ),
                        0, 0, 0);
            }
        }
    }
}
