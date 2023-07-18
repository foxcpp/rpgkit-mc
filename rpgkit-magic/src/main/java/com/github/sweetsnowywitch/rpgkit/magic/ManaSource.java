package com.github.sweetsnowywitch.rpgkit.magic;

import com.github.sweetsnowywitch.rpgkit.magic.components.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface ManaSource {
    void addMana(double value);

    boolean spendMana(double value);

    static @Nullable ManaSource asManaSource(Entity entity) {
        if (entity instanceof ManaSource ms) {
            return ms;
        }
        return ModComponents.MANA.maybeGet(entity).orElse(null);
    }

    static @Nullable ManaSource asManaSource(ServerWorld world, BlockPos pos) {
        var blockEnt = world.getBlockEntity(pos);
        if (blockEnt instanceof ManaSource ms) {
            return ms;
        }
        
        var chunkAreas = world.getChunk(pos).getComponent(ModComponents.CHUNK_MAGIC_EFFECTS);
        return (ManaSource) chunkAreas.getAreas().stream().filter(a -> a instanceof ManaSource).findFirst().orElse(null);
    }
}
