package com.github.foxcpp.rpgkitmc.magic.effects.item;

import com.github.foxcpp.rpgkitmc.magic.effects.ItemEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.items.MagicChargeableItem;
import com.github.foxcpp.rpgkitmc.magic.json.FloatModifier;
import com.github.foxcpp.rpgkitmc.magic.spell.*;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ChargeItemEffect extends ItemEffect {
    public static class Reaction extends SpellReaction {
        private final FloatModifier efficiencyFactor;

        protected Reaction(Type type, JsonObject obj) {
            super(type, obj);
            if (obj.has("efficiency_factor")) {
                this.efficiencyFactor = new FloatModifier(obj);
            } else {
                this.efficiencyFactor = FloatModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof ChargeItemEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("efficiency_factor", this.efficiencyFactor.toJson());
        }
    }

    public class Used extends ItemEffect.Used {
        private final float efficiencyFactor;

        protected Used(SpellBuildCondition.Context ctx) {
            super(ChargeItemEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            var efficiencyFactor = ChargeItemEffect.this.efficiencyFactor;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    efficiencyFactor = r.efficiencyFactor.apply(efficiencyFactor);
                }
            }
            this.efficiencyFactor = efficiencyFactor;
        }

        protected Used(JsonObject obj) {
            super(ChargeItemEffect.this, obj);
            this.efficiencyFactor = obj.get("efficiency_factor").getAsFloat();
        }

        @Override
        public @NotNull TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
            if (!(stack.getItem() instanceof MagicChargeableItem mci)) {
                return TypedActionResult.pass(stack);
            }

            mci.addMagicCharge(stack, SpellElement.COST_MAGICAE, cast.getCost(SpellElement.COST_MAGICAE));

            for (var el : cast.getFullRecipe()) {
                if (el instanceof Aspect asp) {
                    mci.addMagicCharge(stack, asp.id.toString(), el.getBaseCost(SpellElement.COST_MAGICAE));
                }
            }

            return TypedActionResult.success(stack);
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("efficiency_factor", this.efficiencyFactor);
        }
    }

    private final float efficiencyFactor;

    protected ChargeItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("efficiency_factor")) {
            this.efficiencyFactor = obj.get("efficiency_factor").getAsFloat();
        } else {
            this.efficiencyFactor = 0;
        }
    }

    @Override
    public ItemEffect.@NotNull Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public ItemEffect.@NotNull Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("efficiency_factor", this.efficiencyFactor);
    }
}
