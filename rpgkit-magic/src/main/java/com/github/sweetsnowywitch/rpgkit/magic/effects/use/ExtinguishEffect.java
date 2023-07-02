package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ExtinguishEffect extends SimpleUseEffect {
    public ExtinguishEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    public @NotNull ActionResult useOnBlock(ServerSpellCast cast, UseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        if (this.extinguish(cast, world, pos)) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public @NotNull ActionResult useOnEntity(ServerSpellCast cast, UseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        entity.setOnFire(false);
        entity.setOnFireFor(0);
        return ActionResult.SUCCESS;
    }

    private boolean extinguish(ServerSpellCast cast, ServerWorld world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (CampfireBlock.isLitCampfire(blockState) || CandleBlock.isLitCandle(blockState) || CandleCakeBlock.isLitCandle(blockState)) {
            world.setBlockState(pos, blockState.with(Properties.LIT, false), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_CHANGE, pos);
            return true;
        }

        if (blockState.isIn(BlockTags.FIRE)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_DESTROY, pos);
            return true;
        }
        return false;
    }
}
