package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoneMealEffect extends SimpleUseEffect {
    public BoneMealEffect(Identifier id) {
        super(id);
    }

    public BoneMealEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    @Override
    public @NotNull ActionResult useOnBlock(ServerSpellCast cast, SimpleUseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        if (this.boneMeal(cast, world, pos, direction)) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public @NotNull ActionResult useOnEntity(ServerSpellCast cast, SimpleUseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        return ActionResult.PASS;
    }

    private boolean boneMeal(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction side) {
        var mockStack = new ItemStack(Items.BONE_MEAL);
        if (BoneMealItem.useOnFertilizable(mockStack, world, pos)) {
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
            return true;
        }
        BlockPos up = pos.offset(side);
        BlockState blockState = world.getBlockState(up);
        if (blockState.isSideSolidFullSquare(world, pos, side) &&
                BoneMealItem.useOnGround(mockStack, world, up, side)) {
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, up, 0);
            return true;
        }
        return false;
    }
}
