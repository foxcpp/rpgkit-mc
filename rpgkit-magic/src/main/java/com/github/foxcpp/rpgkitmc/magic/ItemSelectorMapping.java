package com.github.foxcpp.rpgkitmc.magic;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

public class ItemSelectorMapping implements ItemMapping {
    protected record Entry(float weight, @NotNull Item item, ImmutableList<LootFunction> functions) {
    }

    protected final ImmutableList<Entry> entries;

    public ItemSelectorMapping(ImmutableList<Entry> entries) {
        this.entries = entries;
    }

    public ItemSelectorMapping(JsonArray arr) {
        this.entries = this.fromJsonArray(arr);
    }

    private ImmutableList<Entry> fromJsonArray(JsonArray arr) {
        ImmutableList.Builder<Entry> mappings = ImmutableList.builder();
        for (var element : arr) {
            var elementObj = element.getAsJsonObject();

            Item item;
            if (elementObj.has("item")) {
                item = Registries.ITEM.getCodec().parse(JsonOps.INSTANCE, elementObj.get("item")).result().orElseThrow();
            } else {
                throw new IllegalArgumentException("item field is required for ItemSelectorMapping");
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

            mappings.add(new Entry(weight, item, functions));
        }
        return mappings.build();
    }

    public ItemStack apply(ItemStack stack, LootContext context) {
        var pick = RPGKitMagicMod.RANDOM.nextFloat();

        var weightAccum = 0f;
        for (var mapping : this.entries) {
            weightAccum += mapping.weight;
            if (weightAccum < pick) {
                continue;
            }

            stack = new ItemStack(mapping.item);
            for (var func : mapping.functions) {
                func.apply(stack, context);
            }
            break;
        }

        return stack;
    }

    public JsonArray toJsonArray() {
        var arr = new JsonArray();
        for (var ent : this.entries) {
            var entObj = new JsonObject();
            entObj.addProperty("weight", ent.weight);
            entObj.addProperty("item", Registries.ITEM.getId(ent.item).toString());
            if (ent.functions != null && ent.functions.size() > 0) {
                var gson = LootGsons.getFunctionGsonBuilder().create();
                var funcArr = new JsonArray();
                for (var func : ent.functions) {
                    funcArr.add(gson.toJsonTree(func, LootFunction.class));
                }
                entObj.add("functions", arr);
            }
            arr.add(entObj);
        }
        return arr;
    }
}
