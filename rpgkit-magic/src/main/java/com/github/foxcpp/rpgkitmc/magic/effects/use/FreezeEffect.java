package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.json.IntModifier;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FreezeEffect extends SimpleUseEffect {
    public static class Reaction extends SpellReaction {
        private final IntModifier duration;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("duration")) {
                this.duration = new IntModifier(obj.get("duration"));
            } else {
                this.duration = new IntModifier(5, 0);
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof FreezeEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("duration", this.duration.toJson());
        }
    }

    private final int duration;

    public FreezeEffect(Identifier id) {
        super(id);
        this.duration = 5;
    }

    public FreezeEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("duration")) {
            this.duration = obj.get("duration").getAsInt();
        } else {
            this.duration = 10;
        }
    }

    @Override
    protected @NotNull ActionResult useOnBlock(ServerSpellCast cast, SimpleUseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        return ActionResult.PASS;
    }

    @Override
    protected @NotNull ActionResult useOnEntity(ServerSpellCast cast, SimpleUseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        var duration = this.duration;
        for (var reaction : reactions) {
            if (reaction instanceof Reaction r) {
                duration = r.duration.applyMultiple(duration, used.reactionStackSize);
            }
        }

        entity.setFrozenTicks(duration);
        return ActionResult.SUCCESS;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("duration", duration);
    }
}
