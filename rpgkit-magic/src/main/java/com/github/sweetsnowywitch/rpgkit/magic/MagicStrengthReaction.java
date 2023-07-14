package com.github.sweetsnowywitch.rpgkit.magic;

import com.github.sweetsnowywitch.rpgkit.magic.json.FloatModifier;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public class MagicStrengthReaction extends SpellReaction {
    public final FloatModifier magicStrength;

    protected MagicStrengthReaction(Type type, JsonObject obj) {
        super(type, obj);
        if (obj.has("magic_strength")) {
            this.magicStrength = new FloatModifier(obj.get("magic_strength"));
        } else {
            this.magicStrength = FloatModifier.NOOP;
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.add("magic_strength", this.magicStrength.toJson());
    }
}
