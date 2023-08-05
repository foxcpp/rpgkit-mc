package com.github.foxcpp.rpgkitmc.magic.blocks;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public final static Block MAGIC_LIGHT = new MagicLightBlock(FabricBlockSettings.copyOf(Blocks.BLACK_WOOL).
            notSolid().noCollision().luminance(15).ticksRandomly());

    public static void register() {
        Registry.register(Registries.BLOCK, new Identifier(RPGKitMagicMod.MOD_ID, "magic_light"), MAGIC_LIGHT);
    }
}
