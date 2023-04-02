package com.github.sweetsnowywitch.csmprpgkit.classes.perks;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModPerks {
    public static final Perk STATUS_EFFECT = new StatusEffectPerk(Identifier.of(RPGKitMod.MOD_ID, "status_effect"));

    public static void register() {
        Registry.register(ModRegistries.CLASS_PERKS, STATUS_EFFECT.typeId, STATUS_EFFECT);
    }
}
