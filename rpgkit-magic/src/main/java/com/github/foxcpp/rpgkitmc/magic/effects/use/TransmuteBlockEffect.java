package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.BlockStateMapping;
import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.events.MagicBlockEvents;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TransmuteBlockEffect extends SimpleUseEffect {
    protected final BlockStateMapping mapping;

    public TransmuteBlockEffect(Identifier id) {
        super(id);
        this.mapping = null;
    }

    public TransmuteBlockEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (!obj.has("mapping")) {
            throw new IllegalArgumentException("missing mapping field for TransmuteItemEffect");
        }

        var mapping = MagicRegistries.TRANSMUTE_MAPPINGS.get(new Identifier(obj.get("mapping").getAsString()));
        if (mapping == null) {
            throw new IllegalArgumentException("transmute mapping does not exist: " + obj.get("mapping").getAsString());
        }

        if (!(mapping instanceof BlockStateMapping bsMapping)) {
            throw new IllegalArgumentException("transmute mapping used in TransmuteBlockEffect should have type=block");
        }
        this.mapping = bsMapping;
    }

    @Override
    protected @NotNull ActionResult useOnBlock(ServerSpellCast cast, Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        if (this.mapping == null) {
            return ActionResult.PASS;
        }

        var bs = world.getBlockState(pos);
        var newBs = this.mapping.apply(bs);

        if (bs.equals(newBs)) {
            return ActionResult.PASS;
        }

        var eventResult = MagicBlockEvents.DAMAGE.invoker().onBlockMagicDamaged(cast, used, world, pos);
        if (eventResult.equals(ActionResult.FAIL) || eventResult.equals(ActionResult.CONSUME) || eventResult.equals(ActionResult.CONSUME_PARTIAL)) {
            return eventResult;
        }

        if (!world.setBlockState(pos, newBs)) {
            return ActionResult.PASS;
        }

        return ActionResult.SUCCESS;
    }
}
