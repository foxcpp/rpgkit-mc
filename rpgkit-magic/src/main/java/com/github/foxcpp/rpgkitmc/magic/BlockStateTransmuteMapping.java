package com.github.foxcpp.rpgkitmc.magic;

import com.github.foxcpp.rpgkitmc.magic.json.BlockStatePredicate;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BlockStateTransmuteMapping implements ItemMapping, BlockStateMapping {
    protected record Entry(BlockStatePredicate predicate,
                           float weight,
                           @Nullable Supplier<BlockState> replacement) {

        @Override
        public String toString() {
            var builder = new StringBuilder();
            builder.append("BlockStateTransmuteMapping.Entry[");
            builder.append(this.predicate);
            builder.append(" -> (");
            builder.append(this.weight);
            builder.append(") ");
            if (this.replacement != null) {
                builder.append(this.replacement.get().toString());
            } else {
                builder.append("no-op");
            }
            builder.append("]");
            return builder.toString();
        }
    }

    public final Identifier id;
    private final ImmutableList<Entry> mappings;
    private final @Nullable Entry defaultEntry;

    public static BlockStateTransmuteMapping ofBlockState(Identifier id, @NotNull JsonObject obj) {
        ImmutableList.Builder<Entry> mappings = ImmutableList.builder();
        for (var element : obj.getAsJsonArray("mappings")) {
            var elementObj = element.getAsJsonObject();

            if (!elementObj.has("predicate")) {
                throw new IllegalArgumentException("block transmute mapping should contain predicate");
            }
            var predicate = new BlockStatePredicate(elementObj.get("predicate"));

            BlockState replacement;
            if (elementObj.has("replacement")) {
                var blockId = new Identifier(elementObj.get("replacement").getAsString());
                replacement = Registries.BLOCK.get(blockId).getDefaultState();
            } else if (elementObj.has("blockstate")) {
                replacement = BlockState.CODEC.parse(JsonOps.INSTANCE, elementObj.get("blockstate")).result().orElseThrow();
            } else {
                replacement = Blocks.STONE.getDefaultState();
            }

            var weight = 1f;
            if (elementObj.has("weight")) {
                weight = elementObj.get("weight").getAsFloat();
            }

            mappings.add(new Entry(predicate, weight,
                    replacement != null ? () -> replacement : null));
        }

        Entry defaultEnt = null;
        if (obj.has("default")) {
            var elementObj = obj.getAsJsonObject("default");

            BlockState replacement;
            if (elementObj.has("block")) {
                var blockId = new Identifier(elementObj.get("block").getAsString());
                replacement = Registries.BLOCK.get(blockId).getDefaultState();
            } else if (elementObj.has("blockstate")) {
                replacement = BlockState.CODEC.parse(JsonOps.INSTANCE, elementObj.get("blockstate")).result().orElseThrow();
            } else {
                replacement = Blocks.STONE.getDefaultState();
            }

            defaultEnt = new Entry(null, 1,
                    replacement != null ? () -> replacement : null);
        }

        return new BlockStateTransmuteMapping(id, mappings.build(), defaultEnt);
    }

    public BlockStateTransmuteMapping(Identifier id, @NotNull ImmutableList<Entry> mappings, @Nullable Entry defaultEnt) {
        this.id = id;
        this.mappings = mappings;
        this.defaultEntry = defaultEnt;
    }

    @Override
    public BlockState apply(BlockState in) {
        var candidateTotalWeight = 0f;
        for (var mapping : this.mappings) {
            if (mapping.predicate.test(in)) {
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
            if (!mapping.predicate.test(in)) {
                continue;
            }

            weightAccum += mapping.weight / candidateTotalWeight;
            if (weightAccum < pick) {
                continue;
            }

            if (mapping.replacement != null) {
                in = mapping.replacement.get();
            }

            noMatch = false;
            break;
        }

        if (noMatch && this.defaultEntry != null) {
            if (this.defaultEntry.replacement != null) {
                in = this.defaultEntry.replacement.get();
            }
        }

        return in;
    }

    public ItemStack apply(ItemStack in, LootContext context) {
        if (!(in.getItem() instanceof BlockItem bi)) {
            return in;
        }

        var bs = bi.getBlock().getDefaultState();
        var stackNbt = in.getNbt();
        if (stackNbt != null) {
            var blockStateNbt = stackNbt.getCompound(BlockItem.BLOCK_STATE_TAG_KEY);
            var stateManager = bi.getBlock().getStateManager();
            for (String name : blockStateNbt.getKeys()) {
                var property = stateManager.getProperty(name);
                if (property == null) continue;
                bs = with(bs, property, blockStateNbt.getString(name));
            }
        }

        bs = this.apply(bs);

        var blockStateNbt = new NbtCompound();
        // Can't be generalized because getProperties() T does not match bs.get T.
        for (Property property : bs.getProperties()) {
            if (bs.contains(property)) {
                blockStateNbt.putString(property.getName(), property.name(bs.get(property)));
            }
        }

        var stack = new ItemStack(bs.getBlock().asItem(), 1);
        if (blockStateNbt.getSize() > 0) {
            stack.setSubNbt(BlockItem.BLOCK_STATE_TAG_KEY, blockStateNbt);
        }

        return stack;
    }

    private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String name) {
        return property.parse(name).map((value) -> state.with(property, value)).orElse(state);
    }
}
