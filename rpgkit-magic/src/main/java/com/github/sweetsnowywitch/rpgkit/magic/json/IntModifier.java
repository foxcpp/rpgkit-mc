package com.github.sweetsnowywitch.rpgkit.magic.json;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class IntModifier implements JsonHelpers.JsonSerializable {
    public static final IntModifier NOOP = new IntModifier(0, 1);

    private final int add;
    private final int mul;

    public IntModifier(int add) {
        this.add = add;
        this.mul = 1;
    }

    public IntModifier(int add, int mul) {
        this.add = add;
        this.mul = mul;
    }

    public IntModifier(JsonElement el) {
        if (el instanceof JsonObject obj) {
            if (obj.has("add")) {
                this.add = obj.get("add").getAsInt();
            } else {
                this.add = 0;
            }

            if (obj.has("mul")) {
                this.mul = obj.get("mul").getAsInt();
            } else {
                this.mul = 1;
            }
        } else if (el instanceof JsonPrimitive p) {
            this.add = p.getAsInt();
            this.mul = 1;
        } else if (el == null || el instanceof JsonNull) {
            this.add = 0;
            this.mul = 1;
        } else {
            throw new IllegalArgumentException("malformed modifier");
        }
    }

    public int apply(int val) {
        return val * this.mul + this.add;
    }

    public int applyMultiple(int val, int count) {
        // (val * mul + add) * mul + add
        // ((val * mul + add) * mul + add) * mul + add
        // (val * mul^3 + add*mul^2 + add*mul + add
        // and so on.

        var res = 0;
        var mulFact = 1;
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
