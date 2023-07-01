package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class PickUpItemEffect extends SimpleUseEffect {
    public PickUpItemEffect(Identifier id) {
        super(id);
    }

    public PickUpItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    @Override
    protected ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        return ActionResult.PASS;
    }

    @Override
    protected ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions) {
        if (entity instanceof ItemEntity ie) {
            ie.setVelocity(
                    cast.getOriginPos().x - entity.getX(),
                    cast.getOriginPos().y - entity.getY(),
                    cast.getOriginPos().z - entity.getZ());
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
