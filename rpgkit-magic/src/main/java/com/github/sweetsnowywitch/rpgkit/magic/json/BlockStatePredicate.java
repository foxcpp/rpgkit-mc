package com.github.sweetsnowywitch.rpgkit.magic.json;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlockStatePredicate implements Predicate<BlockState>, JsonHelpers.JsonElementSerializable {
    protected final @Nullable Block block;
    protected final @Nullable TagKey<Block> tag;
    protected final @Nullable List<BlockStatePredicate> or;

    public BlockStatePredicate(JsonElement el) {
        if (el instanceof JsonPrimitive p) {
            var str = p.getAsString();
            if (str.startsWith("#")) {
                var tag = TagKey.of(Registry.BLOCK_KEY, new Identifier(str.substring(1)));

                this.block = null;
                this.tag = tag;
            } else {
                this.block = Registry.BLOCK.get(new Identifier(str));
                this.tag = null;
            }
            this.or = null;
        } else if (el instanceof JsonArray arr) {
            var predicates = new ArrayList<BlockStatePredicate>(arr.size());
            for (var arrEl : arr) {
                predicates.add(new BlockStatePredicate(arrEl));
            }

            this.block = null;
            this.tag = null;
            this.or = predicates;
        } else if (el.isJsonNull()) {
            this.block = null;
            this.tag = null;
            this.or = null;
        } else {
            throw new IllegalArgumentException("unexpected json value");
        }
    }

    @Override
    public @NotNull JsonElement toJson() {
        if (this.block != null) {
            return new JsonPrimitive(Registry.BLOCK.getId(this.block).toString());
        } else if (this.tag != null) {
            return new JsonPrimitive("#" + this.tag.id().toString());
        } else if (this.or != null) {
            var jsonArr = new JsonArray(this.or.size());
            for (var el : this.or) {
                jsonArr.add(el.toJson());
            }
            return jsonArr;
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public boolean test(BlockState blockState) {
        if (this.block != null) {
            return blockState.isOf(this.block);
        }

        if (this.tag != null) {
            return blockState.isIn(this.tag);
        }

        if (this.or != null) {
            for (var el : this.or) {
                if (el.test(blockState)) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }
}
