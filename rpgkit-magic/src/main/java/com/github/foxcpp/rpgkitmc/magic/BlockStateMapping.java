package com.github.foxcpp.rpgkitmc.magic;

import net.minecraft.block.BlockState;

public interface BlockStateMapping {
    BlockState apply(BlockState bs);
}
