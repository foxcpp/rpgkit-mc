package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.json.BlockStatePredicate;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BreakBlockEffect extends SimpleUseEffect {
    private final boolean drop;

    private final @Nullable BlockStatePredicate filter;

    public BreakBlockEffect(Identifier id) {
        super(id);
        this.drop = false;
        this.filter = null;
    }

    public BreakBlockEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        this.drop = obj.has("drop") && obj.get("drop").getAsBoolean();
        if (obj.has("filter")) {
            this.filter = new BlockStatePredicate(obj.get("filter"));
        } else {
            this.filter = null;
        }
    }

    @Override
    protected ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        if (this.filter != null && !this.filter.test(world.getBlockState(pos))) {
            return ActionResult.PASS;
        }

        if (world.breakBlock(pos, this.drop, cast.getCaster(world))) {
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    protected ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions) {
        var world = (ServerWorld) entity.getWorld();

        if (this.filter != null && !this.filter.test(world.getBlockState(entity.getBlockPos()))) {
            return ActionResult.PASS;
        }

        if (world.breakBlock(entity.getBlockPos(), this.drop, cast.getCaster(world))) {
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("drop", this.drop);
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
    }
}
