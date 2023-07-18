package com.github.sweetsnowywitch.rpgkit.magic.json;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.google.gson.*;
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
    protected final @Nullable BlockStatePredicate not;

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
            this.not = null;
        } else if (el instanceof JsonObject obj) {
            if (obj.has("not")) {
                this.not = new BlockStatePredicate(obj.get("not"));
                this.block = null;
                this.tag = null;
            } else {
                this.not = null;
                if (obj.has("block")) {
                    this.block = Registry.BLOCK.get(new Identifier(obj.get("block").getAsString()));
                    this.tag = null;
                } else if (obj.has("tag")) {
                    var tag = TagKey.of(Registry.BLOCK_KEY, new Identifier(obj.get("tag").getAsString()));

                    this.block = null;
                    this.tag = tag;
                } else {
                    throw new IllegalArgumentException("block or tag is required for BlockStatePredicate");
                }
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
            this.not = null;
        } else if (el.isJsonNull()) {
            this.block = null;
            this.tag = null;
            this.or = null;
            this.not = null;
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
        } else if (this.not != null) {
            var jsonObj = new JsonObject();
            jsonObj.add("not", this.not.toJson());
            return jsonObj;
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public boolean test(BlockState blockState) {
        if (this.not != null) {
            return !this.not.test(blockState);
        }

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

    @Override
    public String toString() {
        if (this.block != null) {
            return "BlockStatePredicate[" + Registry.BLOCK.getId(this.block) + "]";
        }
        if (this.tag != null) {
            return "BlockStatePredicate[#" + this.tag.id() + "]";
        }
        if (this.or != null) {
            var builder = new StringBuilder();
            builder.append("BlockStatePredicate[");
            for (var pred : this.or) {
                if (pred.block != null) {
                    builder.append(Registry.BLOCK.getId(pred.block));
                } else if (pred.tag != null) {
                    builder.append("#").append(pred.tag.id());
                } else {
                    builder.append(pred);
                }
                builder.append(", ");
            }
            builder.append("]");
            return builder.toString();
        }
        return "BlockStatePredicate[empty]";
    }
}
