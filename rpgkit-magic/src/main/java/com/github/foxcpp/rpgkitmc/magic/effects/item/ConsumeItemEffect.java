package com.github.foxcpp.rpgkitmc.magic.effects.item;

import com.github.foxcpp.rpgkitmc.magic.effects.ItemEffect;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ConsumeItemEffect extends ItemEffect {
    public class Used extends ItemEffect.Used {

        protected Used(SpellBuildCondition.Context ctx) {
            super(ConsumeItemEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
        }

        protected Used(JsonObject obj) {
            super(ConsumeItemEffect.this, obj);
        }

        @Override
        public @NotNull TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
            if (stack.isEmpty()) {
                return TypedActionResult.pass(stack);
            }

            if (ConsumeItemEffect.this.ingredient != null && !ConsumeItemEffect.this.ingredient.test(stack)) {
                return TypedActionResult.pass(stack);
            }

            if (ConsumeItemEffect.this.amount == 0) {
                return TypedActionResult.success(ItemStack.EMPTY.copy());
            }
            stack.decrement(ConsumeItemEffect.this.amount);
            return TypedActionResult.success(stack);
        }
    }

    private final int amount;
    private final @Nullable Ingredient ingredient;

    protected ConsumeItemEffect(Identifier id) {
        super(id);
        this.amount = 0;
        this.ingredient = null;
    }

    protected ConsumeItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("amount")) {
            this.amount = obj.get("amount").getAsInt();
        } else {
            this.amount = 1;
        }
        if (obj.has("filter")) {
            this.ingredient = Ingredient.fromJson(obj.get("filter"));
        } else {
            this.ingredient = null;
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
        obj.addProperty("amount", this.amount);
        if (this.ingredient != null) {
            obj.add("filter", this.ingredient.toJson());
        }
    }
}
