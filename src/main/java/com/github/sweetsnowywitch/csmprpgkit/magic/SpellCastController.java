package com.github.sweetsnowywitch.csmprpgkit.magic;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface SpellCastController {
    void startBuild();

    void addElement(int index);

    void switchCatalystBag();

    void performSelfCast();

    void performAreaCast();

    void performItemCast();

    void performCastOnBlock(BlockPos pos, Direction direction);

    void performCastOnEntity(Entity target);

    void performRangedCast();

    void interruptChanneling();
}
