package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;

import java.util.List;

public class FireEffect extends SimpleUseEffect {
    public FireEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    public ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        if (this.lit(cast, world, pos, direction)) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions) {
        entity.setOnFire(true);
        entity.setOnFireFor(5);
        return ActionResult.SUCCESS;
    }

    private boolean lit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        BlockState blockState = world.getBlockState(pos);
        if (CampfireBlock.canBeLit(blockState) || CandleBlock.canBeLit(blockState) || CandleCakeBlock.canBeLit(blockState)) {
            world.setBlockState(pos, blockState.with(Properties.LIT, true), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_CHANGE, pos);
            return true;
        }

        pos = pos.offset(dir);
        if (AbstractFireBlock.canPlaceAt(world, pos, dir)) {
            BlockState blockState2 = AbstractFireBlock.getState(world, pos);
            world.setBlockState(pos, blockState2, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_PLACE, pos);
            return true;
        }
        return false;
    }
}
