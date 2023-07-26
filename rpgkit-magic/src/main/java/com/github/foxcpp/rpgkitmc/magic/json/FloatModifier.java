package com.github.foxcpp.rpgkitmc.magic.json;

import com.github.foxcpp.rpgkitmc.JsonHelpers;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class FloatModifier implements JsonHelpers.JsonSerializable {
    public static final FloatModifier NOOP = new FloatModifier(0, 1);
    public static final FloatModifier ZEROED = new FloatModifier(0, 0);

    private final float add;
    private final float mul;

    public FloatModifier(float add) {
        this.add = add;
        this.mul = 1;
    }

    public FloatModifier(float add, float mul) {
        this.add = add;
        this.mul = mul;
    }

    public FloatModifier(JsonElement el) {
        if (el instanceof JsonObject obj) {
            if (obj.has("add")) {
                this.add = obj.get("add").getAsFloat();
            } else {
                this.add = 0;
            }

            if (obj.has("mul")) {
                this.mul = obj.get("mul").getAsFloat();
            } else {
                this.mul = 1;
            }
        } else if (el instanceof JsonPrimitive p) {
            this.add = p.getAsFloat();
            this.mul = 1;
        } else if (el == null || el instanceof JsonNull) {
            this.add = 0;
            this.mul = 1;
        } else {
            throw new IllegalArgumentException("malformed modifier");
        }
    }

    public float apply(float val) {
        return val * this.mul + this.add;
    }

    public float applyMultiple(float val, int count) {
        // (val * mul + add) * mul + add
        // ((val * mul + add) * mul + add) * mul + add
        // (val * mul^3 + add*mul^2 + add*mul + add
        // and so on.

        float res = 0;
        float mulFact = 1;
        for (int i = 0; i < count; i++) {
            res += add * mulFact;
            mulFact *= this.mul;
        }

        res += val * mulFact;
        return res;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        obj.addProperty("add", this.add);
        obj.addProperty("mul", this.mul);
    }
}

