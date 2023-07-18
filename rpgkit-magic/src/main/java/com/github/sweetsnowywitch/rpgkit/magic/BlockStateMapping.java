package com.github.sweetsnowywitch.rpgkit.magic;

import net.minecraft.block.BlockState;

public interface BlockStateMapping {
    BlockState apply(BlockState bs);
}
