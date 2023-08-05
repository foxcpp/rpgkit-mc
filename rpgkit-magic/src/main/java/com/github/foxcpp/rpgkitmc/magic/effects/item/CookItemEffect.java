package com.github.foxcpp.rpgkitmc.magic.effects.item;

import com.github.foxcpp.rpgkitmc.magic.effects.ItemEffect;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class CookItemEffect extends ItemEffect {
    protected final RecipeType<? extends Recipe<Inventory>> recipeType;
    protected final @Nullable Ingredient filter;

    protected CookItemEffect(Identifier id) {
        super(id);
        this.recipeType = RecipeType.SMELTING;
        this.filter = null;
    }

    protected CookItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("recipe_type")) {
            var recipeType = Registries.RECIPE_TYPE.get(new Identifier(obj.get("recipe_type").getAsString()));
            if (recipeType == null) {
                throw new IllegalArgumentException("unknown recipe_type: " + obj.get("recipe_type"));
            }
            this.recipeType = (RecipeType<? extends Recipe<Inventory>>) recipeType;
        } else {
            this.recipeType = RecipeType.SMELTING;
        }
        if (obj.has("filter")) {
            this.filter = Ingredient.fromJson(obj.get("filter"));
        } else {
            this.filter = null;
        }
    }

    public class Used extends ItemEffect.Used {
        protected Used(SpellBuildCondition.Context ctx) {
            super(CookItemEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
        }

        protected Used(JsonObject obj) {
            super(CookItemEffect.this, obj);
        }

        @Override
        public @NotNull TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
            var result = world.getRecipeManager().getFirstMatch(CookItemEffect.this.recipeType, new SimpleInventory(stack), world);
            if (result.isEmpty()) {
                return TypedActionResult.pass(stack);
            }
            var resStack = result.get().craft(new SimpleInventory(stack), world.getRegistryManager());
            return TypedActionResult.success(resStack);
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
        obj.addProperty("recipe_type", Objects.requireNonNull(Registries.RECIPE_TYPE.getId(this.recipeType)).toString());
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
    }
}
