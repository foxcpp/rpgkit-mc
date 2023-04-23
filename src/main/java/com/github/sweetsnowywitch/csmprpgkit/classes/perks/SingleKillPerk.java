package com.github.sweetsnowywitch.csmprpgkit.classes.perks;

import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

public class SingleKillPerk extends Perk {
    public SingleKillPerk(Identifier typeId) {
        super(typeId);
    }

    @Override
    public Perk withParametersFromJSON(JsonObject jsonObject) {
        return null;
    }

    @Override
    public JsonObject parametersToJSON() {
        return null;
    }
}
