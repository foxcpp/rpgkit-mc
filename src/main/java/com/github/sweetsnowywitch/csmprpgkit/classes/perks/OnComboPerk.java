package com.github.sweetsnowywitch.csmprpgkit.classes.perks;

import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

public class OnComboPerk extends Perk {
    public static final int DEFAULT_COMBO = Integer.MAX_VALUE;

    private int combo;

    private int maxCombo;

    public OnComboPerk(Identifier typeId) {
        super(typeId);
        this.combo = DEFAULT_COMBO;
        this.maxCombo = DEFAULT_COMBO;
    }

    public OnComboPerk(Identifier typeId, int combo, int maxCombo) {
        super(typeId);
        this.combo = combo;
        this.maxCombo = maxCombo;
    }

    public int getCombo(){
        return this.combo;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    @Override
    public Perk withParametersFromJSON(JsonObject obj) {
        if (obj.has("combo")) {
            this.combo = obj.get("combo").getAsInt();
            this.maxCombo = obj.get("maxCombo").getAsInt();
        }
        return new OnComboPerk(this.typeId, this.combo, this.maxCombo);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = new JsonObject();
        obj.addProperty("combo", this.combo);
        obj.addProperty("maxCombo", this.maxCombo);
        return obj;
    }
}
