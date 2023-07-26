package com.github.foxcpp.rpgkitmc.classes;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Perk with tick() method that gets called every server tick.
 */
public interface ServerTickablePerk {
    void tick(ServerPlayerEntity entity);
}
