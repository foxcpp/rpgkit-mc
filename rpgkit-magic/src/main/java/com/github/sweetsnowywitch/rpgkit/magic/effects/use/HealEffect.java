package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.effects.SpellEffect;
import com.github.sweetsnowywitch.rpgkit.magic.json.DoubleModifier;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HealEffect extends SimpleUseEffect {
    public static class Reaction extends SpellReaction {
        private final DoubleModifier amount;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("amount")) {
                this.amount = new DoubleModifier(obj.get("amount"));
            } else {
                this.amount = new DoubleModifier(5, 0);
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof HealEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("amount", this.amount.toJson());
        }
    }

    private final double amount;

    public HealEffect(Identifier id) {
        super(id);
        this.amount = 5;
    }

    public HealEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("amount")) {
            this.amount = obj.get("amount").getAsFloat();
        } else {
            this.amount = 10;
        }
    }

    @Override
    protected @NotNull ActionResult useOnBlock(ServerSpellCast cast, SimpleUseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        return ActionResult.PASS;
    }

    @Override
    protected @NotNull ActionResult useOnEntity(ServerSpellCast cast, SimpleUseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        var amount = this.amount;
        for (var reaction : reactions) {
            if (reaction instanceof Reaction r) {
                amount = r.amount.applyMultiple(amount, used.reactionStackSize);
            }
        }

        if (entity instanceof LivingEntity le && !le.isDead()) {
            le.heal((float) amount);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("amount", amount);
    }
}
