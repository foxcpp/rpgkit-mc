package com.github.sweetsnowywitch.rpgkit.magic.components.chunk;

import com.github.sweetsnowywitch.rpgkit.magic.MagicArea;
import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.components.ModComponents;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Instant;
import java.util.*;

public class MagicEffectsComponent implements Component, ServerTickingComponent, AutoSyncedComponent {
    private static final Map<ServerPlayerEntity, Instant> lastChangeSynced = new HashMap<>();
    private final Chunk chunk;
    private @Nullable List<MagicArea> areas;
    private @Nullable BitSetVoxelSet hasEffects;
    private Instant lastChange;

    public MagicEffectsComponent(Chunk chunk) {
        this.chunk = chunk;
        this.lastChange = Instant.now();
    }

    public static void addGlobalArea(ServerWorld world, MagicArea area) {
        var visited = new ArrayList<ChunkPos>();
        area.getBox().forEachVertex(vert -> {
            var chunk = world.getChunk(vert);
            if (visited.contains(chunk.getPos())) {
                return;
            }
            chunk.getComponent(ModComponents.CHUNK_MAGIC_EFFECTS).addArea(area);
            visited.add(chunk.getPos());
        });
    }

    public static void removeGlobalArea(ServerWorld world, MagicArea area) {
        var visited = new ArrayList<ChunkPos>();
        area.getBox().forEachVertex(vert -> {
            var chunk = world.getChunk(vert);
            if (visited.contains(chunk.getPos())) {
                return;
            }
            chunk.getComponent(ModComponents.CHUNK_MAGIC_EFFECTS).removeArea(area);
            visited.add(chunk.getPos());
        });
    }

    public void addArea(MagicArea area) {
        if (this.areas == null) {
            this.areas = new ArrayList<>();
        }
        if (this.hasEffects == null) {
            this.hasEffects = new BitSetVoxelSet(16, chunk.getHeight(), 16);
        }

        this.areas.add(area);
        int cnt = this.computeHasEffects(this.hasEffects, area);

        RPGKitMagicMod.LOGGER.info("Created MagicEffectsComponent area of {} in {} with {} affected blocks at {}",
                area.getEffectID(), this.chunk.getPos(), cnt, area.getBox());

        this.lastChange = Instant.now();
        this.chunk.setNeedsSaving(true);
        ModComponents.CHUNK_MAGIC_EFFECTS.sync(this.chunk);
    }

    public void removeAreas(Iterable<? extends MagicArea> areas) {
        if (this.areas == null) {
            return;
        }

        boolean removed = false;
        for (var area : areas) {
            if (this.areas.remove(area)) {
                removed = true;
            }
        }

        if (!removed) {
            return;
        }

        if (this.areas.size() == 0) {
            this.areas = null;
        }
        this.computeHasEffects();

        this.lastChange = Instant.now();
        this.chunk.setNeedsSaving(true);
        ModComponents.CHUNK_MAGIC_EFFECTS.sync(this.chunk);
    }

    public void removeArea(MagicArea area) {
        if (this.areas == null) {
            return;
        }

        if (!this.areas.remove(area)) {
            return;
        }

        if (this.areas.size() == 0) {
            this.areas = null;
        }
        this.computeHasEffects();

        this.lastChange = Instant.now();
        this.chunk.setNeedsSaving(true);
        ModComponents.CHUNK_MAGIC_EFFECTS.sync(this.chunk);
    }

    private int computeHasEffects(@NotNull BitSetVoxelSet set, @NotNull MagicArea area) {
        int count = 0;
        var chunkPos = this.chunk.getPos();
        var box = area.getBox();
        for (int y = Math.max(box.getMinY(), this.chunk.getBottomY()); y <= Math.min(box.getMaxY(), this.chunk.getTopY()); y++) {
            for (int x = Math.max(box.getMinX(), chunkPos.getStartX()); x <= Math.min(box.getMaxX(), chunkPos.getEndX()); x++) {
                for (int z = Math.max(box.getMinZ(), chunkPos.getStartZ()); z <= Math.min(box.getMaxZ(), chunkPos.getEndZ()); z++) {
                    set.set(
                            ChunkSectionPos.getLocalCoord(x),
                            ChunkSectionPos.getLocalCoord(y),
                            ChunkSectionPos.getLocalCoord(z));
                    count++;
                }
            }
        }
        return count;
    }

    private void computeHasEffects() {
        if (this.areas == null || this.areas.size() == 0) {
            this.hasEffects = null;
            return;
        }

        var newHasEffects = new BitSetVoxelSet(16, this.chunk.getHeight(), 16);
        int count = 0;
        for (MagicArea area : this.areas) {
            count += this.computeHasEffects(newHasEffects, area);
        }
        this.hasEffects = newHasEffects;
        RPGKitMagicMod.LOGGER.debug("MagicEffectsComponent.computeHasEffects: {} affected blocks across {} areas in {}", count, this.areas.size(), this.chunk.getPos());
    }

    public List<MagicArea> getAreas(@NotNull BlockPos pos, Identifier effectID) {
        if (this.hasEffects == null) {
            return List.of();
        }
        assert this.areas == null;
        if (!this.hasEffects.contains(ChunkSectionPos.getLocalCoord(pos.getX()),
                ChunkSectionPos.getLocalCoord(pos.getY()),
                ChunkSectionPos.getLocalCoord(pos.getZ()))) {
            return List.of();
        }

        var areasInBlock = new ArrayList<MagicArea>(2);
        for (MagicArea area : this.areas) {
            if (area.getBox().contains(pos) && area.getEffectID().equals(effectID)) {
                areasInBlock.add(area);
            }
        }
        return areasInBlock;
    }

    public <T extends MagicArea> List<T> getAreas(@NotNull BlockPos pos, Class<T> areaType) {
        if (this.hasEffects == null) {
            return List.of();
        }
        assert this.areas == null;

        if (!this.hasEffects.contains(ChunkSectionPos.getLocalCoord(pos.getX()),
                ChunkSectionPos.getLocalCoord(pos.getY()),
                ChunkSectionPos.getLocalCoord(pos.getZ()))) {
            return List.of();
        }

        var areasInBlock = new ArrayList<T>(2);
        for (MagicArea area : this.areas) {
            if (!area.getBox().contains(pos)) {
                continue;
            }
            if (areaType.isInstance(area)) {
                areasInBlock.add(areaType.cast(area));
            }
        }
        return areasInBlock;
    }

    public @Unmodifiable List<MagicArea> getAreas() {
        if (this.areas == null) {
            return List.of();
        }
        return Collections.unmodifiableList(this.areas);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if (!tag.contains("Areas")) {
            return;
        }

        var areasNBT = tag.getList("Areas", NbtElement.COMPOUND_TYPE);
        if (areasNBT.size() == 0) {
            return;
        }

        this.areas = new ArrayList<>(areasNBT.size());
        for (var el : areasNBT) {
            var comp = (NbtCompound) el;

            var effectID = Identifier.tryParse(comp.getString("EffectID"));
            if (effectID == null || effectID.getPath().equals("")) {
                RPGKitMagicMod.LOGGER.error("MagicEffectsComponent loaded with invalid effect identifier {} at chunkpos {}",
                        tag.getString("EffectID"), this.chunk.getPos());
                continue;
            }
            var factory = MagicRegistries.EFFECT_AREAS.get(effectID);
            if (factory == null) {
                factory = MagicArea::new;
            }

            var area = factory.createAreaFromNbt(effectID, comp);
            this.areas.add(area);

            RPGKitMagicMod.LOGGER.debug("Loaded MagicEffectsComponent area of {} in {}", area.getEffectID(), this.chunk.getPos());
        }

        this.computeHasEffects();

        if (this.areas.size() > 0) {
            RPGKitMagicMod.LOGGER.info("MagicEffectsComponent loaded with {} areas at chunkpos {}", this.areas.size(), this.chunk.getPos());
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        if (this.areas == null) {
            return;
        }

        var areasNBT = new NbtList();
        for (MagicArea area : this.areas) {
            var areaNBT = new NbtCompound();
            areaNBT.putString("EffectID", area.getEffectID().toString());
            area.writeToNbt(areaNBT);
            areasNBT.add(areaNBT);
        }
        tag.put("Areas", areasNBT);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        if (this.chunk instanceof WorldChunk wc && !player.getWorld().equals(wc.getWorld())) {
            return false;
        }
        if (this.chunk.getPos().getChebyshevDistance(player.getChunkPos()) > 5) {
            return false;
        }
        if (lastChangeSynced.getOrDefault(player, Instant.MIN).compareTo(this.lastChange) < 0) {
            lastChangeSynced.put(player, this.lastChange);
            return true;
        }
        return false;
    }

    @Override
    public void serverTick() {
        if (this.areas == null) {
            return;
        }

        var needsSaving = false;
        var shouldRemove = false;
        for (int i = 0; i < this.areas.size(); i++) {
            var area = this.areas.get(i);
            if (area.getOwnerChunkPos().equals(this.chunk.getPos())) {
                area.ageTick();
                if (area.isDiscarded()) {
                    RPGKitMagicMod.LOGGER.info("MagicEffectsComponent area at {} of {} expired", area.getBox(), area.getEffectID());
                    this.areas.set(i, null);
                    shouldRemove = true;
                    needsSaving = true;
                }

                if (this.chunk instanceof WorldChunk wc) {
                    area.tick((ServerWorld) wc.getWorld());
                }
                if (area.isDirty()) {
                    needsSaving = true;
                    area.setDirty(false);
                }
            }
        }

        if (shouldRemove) {
            this.areas.removeIf(Objects::isNull);
            this.computeHasEffects();
        }

        if (needsSaving) {
            this.lastChange = Instant.now();
            ModComponents.CHUNK_MAGIC_EFFECTS.sync(this.chunk);
        }

        this.chunk.setNeedsSaving(needsSaving);
    }
}
