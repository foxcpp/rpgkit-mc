package com.github.sweetsnowywitch.csmprpgkit.classes.perks;

import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.google.gson.JsonObject;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ComboPerk extends Perk {
    public static final int DEFAULT_COMBO = Integer.MAX_VALUE;
    public static final int DEFAULT_MODIFIER = 0;

    private final int combo;
    private final int maxCombo;
    private final int modifier;

    public ComboPerk(Identifier typeId) {
        super(typeId);
        this.combo = DEFAULT_COMBO;
        this.maxCombo = DEFAULT_COMBO;
        this.modifier = DEFAULT_MODIFIER;
    }

    public ComboPerk(Identifier typeId, int combo, int maxCombo, int modifier) {
        super(typeId);
        this.combo = combo;
        this.maxCombo = maxCombo;
        this.modifier = modifier;
    }

    @Override
    public Perk withParametersFromJSON(JsonObject obj) {
        var combo = this.combo;
        if (obj.has("combo")) {
            combo = obj.get("combo").getAsInt();
        }

        var maxCombo = this.maxCombo;
        if (obj.has("maxCombo")) {
            maxCombo = obj.get("maxCombo").getAsInt();
        }

        var modifier = this.modifier;
        if (obj.has("modifier")) {
            modifier = obj.get("modifier").getAsInt();
        }
        return new ComboPerk(this.typeId, combo, maxCombo, modifier);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        obj.addProperty("combo", this.combo);
        obj.addProperty("maxCombo", this.maxCombo);
        obj.addProperty("modifier", this.modifier);
        return obj;
    }

    public int getCombo(){
        return this.combo;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public int getModifier() {
        return modifier;
    }
}
