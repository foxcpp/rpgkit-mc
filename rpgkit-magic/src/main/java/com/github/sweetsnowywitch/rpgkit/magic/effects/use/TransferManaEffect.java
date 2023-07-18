package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.ManaSource;
import com.github.sweetsnowywitch.rpgkit.magic.effects.SpellEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.json.FloatModifier;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellElement;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TransferManaEffect extends UseEffect {
    public static class Reaction extends SpellReaction {
        protected final FloatModifier fixedAmount;
        protected final FloatModifier spellFactor;

        protected Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("fixed_amount")) {
                this.fixedAmount = new FloatModifier(obj.get("fixed_amount"));
            } else {
                this.fixedAmount = FloatModifier.NOOP;
            }
            if (obj.has("spell_factor")) {
                this.spellFactor = new FloatModifier(obj.get("spell_factor"));
            } else {
                this.spellFactor = FloatModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof TransferManaEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("fixed_amount", this.fixedAmount.toJson());
            obj.add("spell_factor", this.spellFactor.toJson());
        }
    }

    public class Used extends UseEffect.Used {
        protected final float fixedAmount;
        protected final float spellFactor;

        protected Used(SpellBuildCondition.Context ctx) {
            super(TransferManaEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            var fixedAmount = TransferManaEffect.this.fixedAmount;
            var spellFactor = TransferManaEffect.this.spellFactor;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    fixedAmount = r.fixedAmount.apply(fixedAmount);
                    spellFactor = r.spellFactor.apply(spellFactor);
                }
            }
            this.fixedAmount = fixedAmount;
            this.spellFactor = spellFactor;
        }

        protected Used(JsonObject obj) {
            super(TransferManaEffect.this, obj);
            this.fixedAmount = obj.get("fixed_amount").getAsFloat();
            this.spellFactor = obj.get("spell_factor").getAsFloat();
        }

        @Override
        public @NotNull ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            var mana = ManaSource.asManaSource(world, pos);
            if (mana == null) {
                return ActionResult.PASS;
            }

            if (this.fixedAmount > 0) {
                mana.addMana(this.fixedAmount);
            }
            if (this.spellFactor > 0) {
                mana.addMana(this.spellFactor * cast.getCost(SpellElement.COST_MAGICAE));
            }
            return ActionResult.SUCCESS;
        }

        @Override
        public @NotNull ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            var mana = ManaSource.asManaSource(entity);
            if (mana == null) {
                return ActionResult.PASS;
            }

            if (this.fixedAmount > 0) {
                mana.addMana(this.fixedAmount);
            }
            if (this.spellFactor > 0) {
                mana.addMana(this.spellFactor * cast.getCost(SpellElement.COST_MAGICAE));
            }
            return ActionResult.SUCCESS;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("fixed_amount", this.fixedAmount);
            obj.addProperty("spell_factor", this.spellFactor);
        }
    }

    protected final float fixedAmount;
    protected final float spellFactor;

    public TransferManaEffect(Identifier id) {
        super(id);
        this.fixedAmount = 0;
        this.spellFactor = 0.5f;
    }

    public TransferManaEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("fixed_amount")) {
            this.fixedAmount = obj.get("fixed_amount").getAsFloat();
        } else {
            this.fixedAmount = 0;
        }
        if (obj.has("spell_factor")) {
            this.spellFactor = obj.get("spell_factor").getAsFloat();
        } else {
            this.spellFactor = 0.5f;
        }
    }

    @Override
    public @NotNull Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public @NotNull Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("fixed_amount", this.fixedAmount);
        obj.addProperty("spell_factor", this.spellFactor);
    }
}
