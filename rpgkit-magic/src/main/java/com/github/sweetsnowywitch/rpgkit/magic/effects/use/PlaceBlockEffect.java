package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.json.BlockStatePredicate;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceBlockEffect extends SimpleUseEffect {
    private final BlockState blockState;
    private final boolean replace;
    private final @Nullable BlockStatePredicate filter;

    public PlaceBlockEffect(Identifier id) {
        super(id);
        this.blockState = Blocks.STONE.getDefaultState();
        this.replace = false;
        this.filter = null;
    }

    public PlaceBlockEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("block")) {
            var blockId = new Identifier(obj.get("block").getAsString());
            this.blockState = Registry.BLOCK.get(blockId).getDefaultState();
        } else if (obj.has("blockstate")) {
            this.blockState = BlockState.CODEC.parse(JsonOps.INSTANCE, obj.get("blockstate")).result().orElseThrow();
        } else {
            this.blockState = Blocks.STONE.getDefaultState();
        }

        this.replace = obj.has("replace") && obj.get("replace").getAsBoolean();

        if (obj.has("filter")) {
            this.filter = new BlockStatePredicate(obj.get("filter"));
        } else {
            this.filter = null;
        }
    }

    @Override
    protected ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        BlockPos target;
        if (this.replace) {
            target = pos;
        } else {
            target = pos.add(direction.getVector());
        }

        if (this.filter != null && !this.filter.test(world.getBlockState(target))) {
            return ActionResult.PASS;
        }

        world.setBlockState(target, this.blockState);
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions) {
        var world = (ServerWorld) entity.getWorld();

        BlockPos target;
        if (entity.isOnGround() && this.replace) {
            target = entity.getBlockPos().add(0, -1, 0);
        } else {
            target = entity.getBlockPos();
        }

        if (this.filter != null && !this.filter.test(world.getBlockState(target))) {
            return ActionResult.PASS;
        }

        world.setBlockState(target, this.blockState);
        return ActionResult.SUCCESS;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.add("blockstate", BlockState.CODEC.encodeStart(JsonOps.INSTANCE, this.blockState).result().orElseThrow());
        obj.addProperty("replace", this.replace);
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
    }
}
