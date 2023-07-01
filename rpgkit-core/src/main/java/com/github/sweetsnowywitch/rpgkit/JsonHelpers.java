package com.github.sweetsnowywitch.rpgkit;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class JsonHelpers {
    @FunctionalInterface
    public interface JsonSerializable {
        void toJson(@NotNull JsonObject obj);
    }

    public static <T> ImmutableList<T> fromJsonList(JsonArray input, Function<JsonObject, T> mapper) {
        if (input == null) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (var el : input) {
            builder.add(mapper.apply(el.getAsJsonObject()));
        }
        return builder.build();
    }

    public static <T extends JsonSerializable> JsonArray toJsonList(List<T> input) {
        var arr = new JsonArray(input.size());
        for (var el : input) {
            var obj = new JsonObject();
            el.toJson(obj);
            arr.add(obj);
        }
        return arr;
    }
}
