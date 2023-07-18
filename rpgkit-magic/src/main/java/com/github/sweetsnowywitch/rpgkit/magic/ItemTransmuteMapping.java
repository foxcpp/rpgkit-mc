package com.github.sweetsnowywitch.rpgkit.magic;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemTransmuteMapping implements ItemMapping {
    protected record Entry(Predicate<ItemStack> ingredient, float weight,
                           @Nullable Supplier<ItemStack> replacement,
                           ImmutableList<LootFunction> functions) {
    }

    public final Identifier id;
    private final ImmutableList<Entry> mappings;
    private final @Nullable Entry defaultEntry;

    public static ItemTransmuteMapping ofItemStack(Identifier id, @NotNull JsonObject obj) {
        ImmutableList.Builder<Entry> mappings = ImmutableList.builder();
        for (var element : obj.getAsJsonArray("mappings")) {
            var elementObj = element.getAsJsonObject();

            var ingredient = Ingredient.fromJson(elementObj.get("ingredient"));
            Item replacement;
            if (elementObj.has("replacement")) {
                replacement = Registry.ITEM.getCodec().parse(JsonOps.INSTANCE, elementObj.get("replacement")).result().orElseThrow();
            } else {
                replacement = null;
            }

            var weight = 1f;
            if (elementObj.has("weight")) {
                weight = elementObj.get("weight").getAsFloat();
            }

            ImmutableList<LootFunction> functions = ImmutableList.of();
            if (elementObj.has("functions")) {
                ImmutableList.Builder<LootFunction> functionsBuilder = ImmutableList.builder();
                var gson = LootGsons.getFunctionGsonBuilder().create();
                for (var funcObj : elementObj.getAsJsonArray("functions")) {
                    functionsBuilder.add(gson.fromJson(funcObj, LootFunction.class));
                }
                functions = functionsBuilder.build();
            }

            mappings.add(new Entry(ingredient, weight,
                    replacement != null ? () -> new ItemStack(replacement) : null,
                    functions));
        }

        Entry defaultEnt = null;
        if (obj.has("default")) {
            var elementObj = obj.getAsJsonObject("default");

            Item replacement;
            if (elementObj.has("replacement")) {
                replacement = Registry.ITEM.getCodec().parse(JsonOps.INSTANCE, elementObj.get("replacement")).result().orElseThrow();
            } else {
                replacement = null;
            }

            ImmutableList<LootFunction> functions = ImmutableList.of();
            if (elementObj.has("functions")) {
                ImmutableList.Builder<LootFunction> functionsBuilder = ImmutableList.builder();
                var gson = LootGsons.getFunctionGsonBuilder().create();
                for (var funcObj : elementObj.getAsJsonArray("functions")) {
                    functionsBuilder.add(gson.fromJson(funcObj, LootFunction.class));
                }
                functions = functionsBuilder.build();
            }

            defaultEnt = new Entry(null, 1,
                    replacement != null ? () -> new ItemStack(replacement) : null, functions);
        }

        return new ItemTransmuteMapping(id, mappings.build(), defaultEnt);
    }

    public ItemTransmuteMapping(Identifier id, @NotNull ImmutableList<Entry> mappings, @Nullable Entry defaultEnt) {
        this.id = id;
        this.mappings = mappings;
        this.defaultEntry = defaultEnt;
    }

    public ItemStack apply(ItemStack in, LootContext context) {
        var candidateTotalWeight = 0f;
        for (var mapping : this.mappings) {
            if (mapping.ingredient.test(in)) {
                candidateTotalWeight += mapping.weight;
            }
        }

        if (candidateTotalWeight == 0) {
            return in;
        }

        var pick = RPGKitMagicMod.RANDOM.nextFloat();
        var noMatch = true;
        var weightAccum = 0f;
        for (var mapping : this.mappings) {
            if (!mapping.ingredient.test(in)) {
                continue;
            }

            weightAccum += mapping.weight / candidateTotalWeight;
            if (weightAccum < pick) {
                continue;
            }

            if (mapping.replacement != null) {
                in = mapping.replacement.get();
            }

            for (var func : mapping.functions) {
                func.apply(in, context);
            }

            noMatch = false;
            break;
        }

        if (noMatch && this.defaultEntry != null) {
            if (this.defaultEntry.replacement != null) {
                in = this.defaultEntry.replacement.get();
            }

            for (var func : this.defaultEntry.functions) {
                func.apply(in, context);
            }
        }

        return in;
    }
}
