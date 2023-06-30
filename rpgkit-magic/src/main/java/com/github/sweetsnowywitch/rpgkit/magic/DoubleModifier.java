package com.github.sweetsnowywitch.rpgkit.magic;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class DoubleModifier implements JsonHelpers.JsonSerializable {
    public static final DoubleModifier NOOP = new DoubleModifier(0, 1);

    private final double add;
    private final double mul;

    public DoubleModifier(double add, double mul) {
        this.add = add;
        this.mul = mul;
    }

    public DoubleModifier(JsonElement el) {
        if (el instanceof JsonObject obj) {
            if (obj.has("add")) {
                this.add = obj.get("add").getAsDouble();
            } else {
                this.add = 0;
            }

            if (obj.has("mul")) {
                this.mul = obj.get("mul").getAsDouble();
            } else {
                this.mul = 1;
            }
        } else if (el instanceof JsonPrimitive p) {
            this.add = p.getAsDouble();
            this.mul = 1;
        } else {
            throw new IllegalArgumentException("malformed modifier");
        }
    }

    public double apply(double val) {
        return val * this.mul + this.add;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        obj.addProperty("add", this.add);
        obj.addProperty("mul", this.mul);
    }
}
