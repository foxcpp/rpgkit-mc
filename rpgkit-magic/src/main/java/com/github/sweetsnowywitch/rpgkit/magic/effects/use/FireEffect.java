package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.events.MagicBlockEvents;
import com.github.sweetsnowywitch.rpgkit.magic.json.BlockStatePredicate;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireEffect extends SimpleUseEffect {
    private final boolean lit;
    private final boolean fire;
    private final @Nullable BlockStatePredicate filter;

    public FireEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        this.lit = !obj.has("lit") || obj.get("lit").getAsBoolean();
        this.fire = !obj.has("fire") || obj.get("fire").getAsBoolean();
        if (obj.has("filter")) {
            this.filter = new BlockStatePredicate(obj.get("filter"));
        } else {
            this.filter = null;
        }
    }

    public @NotNull ActionResult useOnBlock(ServerSpellCast cast, SimpleUseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        var eventResult = MagicBlockEvents.DAMAGE.invoker().onBlockMagicDamaged(cast, used, world, pos);
        if (eventResult.equals(ActionResult.FAIL) || eventResult.equals(ActionResult.CONSUME) || eventResult.equals(ActionResult.CONSUME_PARTIAL)) {
            return eventResult;
        }

        if (this.filter != null && !this.filter.test(world.getBlockState(pos))) {
            return ActionResult.PASS;
        }

        if (this.lit(cast, world, pos, direction)) {
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public @NotNull ActionResult useOnEntity(ServerSpellCast cast, SimpleUseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        entity.setOnFire(true);
        entity.setOnFireFor(5);
        return ActionResult.SUCCESS;
    }

    private boolean lit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        BlockState blockState = world.getBlockState(pos);
        if (this.lit && CampfireBlock.canBeLit(blockState) || CandleBlock.canBeLit(blockState) || CandleCakeBlock.canBeLit(blockState)) {
            world.setBlockState(pos, blockState.with(Properties.LIT, true), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_CHANGE, pos);
            return true;
        }

        pos = pos.offset(dir);
        if (this.fire && AbstractFireBlock.canPlaceAt(world, pos, dir)) {
            BlockState blockState2 = AbstractFireBlock.getState(world, pos);
            world.setBlockState(pos, blockState2, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_PLACE, pos);
            return true;
        }
        return false;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("lit", this.lit);
        obj.addProperty("fire", this.fire);
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
    }
}
