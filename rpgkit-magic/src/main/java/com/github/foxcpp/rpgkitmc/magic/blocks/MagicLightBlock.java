package com.github.foxcpp.rpgkitmc.magic.blocks;

import com.github.foxcpp.rpgkitmc.magic.particle.GenericSpellParticleEffect;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class MagicLightBlock extends Block {
    public MagicLightBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.spawnParticles(new GenericSpellParticleEffect(0xFFFFFFF, 10),
                pos.getX(), pos.getY(), pos.getZ(), 3,
                1f, 1f, 1f,
                0);

        if (random.nextInt(15) == 1) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
        // none
    }
}
