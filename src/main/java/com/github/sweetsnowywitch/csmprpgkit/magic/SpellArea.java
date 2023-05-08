package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.TrackedHandlers;
import com.github.sweetsnowywitch.csmprpgkit.particle.GenericSpellParticleEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.function.BiFunction;

public class SpellArea {
    @FunctionalInterface
    public interface Factory {
        SpellArea createAreaFromNbt(Identifier effectID, NbtCompound comp);
    }

    public static Factory factoryFor(BiFunction<Identifier, NbtCompound, SpellArea> nbt) {
        return nbt::apply;
    }

    protected final Identifier effectID;
    protected final BlockBox box;
    protected final ChunkPos ownerChunkPos;
    protected final SpellCast cast;
    protected int age; // XXX: NOT AVAILABLE ON CLIENT-SIDE
    protected final int maxAge;
    protected int particleColor;
    private boolean dirty; // true if anything changed and Area should be saved
    private boolean discarded;

    public SpellArea(BlockBox box, SpellEffect effect, SpellCast cast, int maxAge) {
        this.effectID = effect.id;
        this.box = box;
        this.ownerChunkPos = new ChunkPos(new BlockPos(box.getMinX(), box.getMinX(), box.getMinZ()));
        this.cast = cast;
        this.age = 0;
        this.maxAge = maxAge;
        this.particleColor = SpellElement.calculateBaseColor(cast.getFullRecipe());
        this.dirty = false;
        this.discarded = false;
    }

    public SpellArea(Identifier effectID, NbtCompound tag) {
        this.effectID = effectID;
        this.box = TrackedHandlers.BLOCKBOX_CODEC.parse(NbtOps.INSTANCE, tag.get("Box"))
                .resultOrPartial(RPGKitMod.LOGGER::error).orElseThrow();
        this.ownerChunkPos = new ChunkPos(tag.getLong("OwnerChunkPos"));
        this.cast = SpellCast.readFromNbt(tag.getCompound("Cast"));
        this.age = tag.getInt("Age");
        this.maxAge = tag.getInt("MaxAge");
        this.particleColor = SpellElement.calculateBaseColor(this.cast.getFullRecipe());
        this.dirty = false;
        this.discarded = false;
    }

    public void writeToNbt(NbtCompound tag) {
        tag.put("Box", TrackedHandlers.BLOCKBOX_CODEC.encodeStart(NbtOps.INSTANCE, this.box)
                .resultOrPartial(RPGKitMod.LOGGER::error).orElseThrow());
        tag.putLong("OwnerChunkPos", this.ownerChunkPos.toLong());
        var cast = new NbtCompound();
        this.cast.writeToNbt(cast);
        tag.put("Cast", cast);
        tag.putInt("Age", this.age);
        tag.putInt("MaxAge", this.maxAge);
    }

    public void spawnParticles(ServerWorld world) {
        var volume = this.box.getBlockCountX() * this.box.getBlockCountY() * this.box.getBlockCountZ();

        var centerX = this.box.getMinX() + (float) (this.box.getMaxX() - this.box.getMinX() + 1) / 2;
        var centerY = this.box.getMinY() + (float) (this.box.getMaxY() - this.box.getMinY() + 1) / 2;
        var centerZ = this.box.getMinZ() + (float) (this.box.getMaxZ() - this.box.getMinZ() + 1) / 2;

        var cnt = volume / 64;
        if (cnt == 0) {
            cnt = 2;
        }

        world.spawnParticles(new GenericSpellParticleEffect(this.particleColor, 10),
                centerX, centerY, centerZ, cnt,
                (float) this.box.getBlockCountX() / 2.75, (float) this.box.getBlockCountY() / 2.75, (float) this.box.getBlockCountZ() / 2.75,
                0);
    }

    public void ageTick() {
        if (this.age >= this.getMaxAge()) {
            this.discard();
        }

        this.age++;
    }

    public void tick(ServerWorld world) {
        if (this.age % 6 == 0) {
            this.spawnParticles(world);
        }
    }

    public final SpellCast getCast() {
        return cast;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public final BlockBox getBox() {
        return box;
    }

    public final Identifier getEffectID() {
        return effectID;
    }

    public final ChunkPos getOwnerChunkPos() {
        return ownerChunkPos;
    }

    public final boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void discard() {
        this.discarded = true;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public int getAge() {
        return age;
    }
}
