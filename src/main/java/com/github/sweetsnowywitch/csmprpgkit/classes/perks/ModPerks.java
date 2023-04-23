package com.github.sweetsnowywitch.csmprpgkit.classes.perks;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModPerks {
    public static final Perk STATUS_EFFECT = new StatusEffectPerk(Identifier.of(RPGKitMod.MOD_ID, "status_effect"));
    public static final OnComboPerk ON_COMBO = new OnComboPerk(Identifier.of(RPGKitMod.MOD_ID, "on_combo"));
    public static final KillStreakPerk KILL_STREAK = new KillStreakPerk(Identifier.of(RPGKitMod.MOD_ID, "kill_streak"));

    public static void register() {
        Registry.register(ModRegistries.CLASS_PERKS, STATUS_EFFECT.typeId, STATUS_EFFECT);
        Registry.register(ModRegistries.CLASS_PERKS, ON_COMBO.typeId, ON_COMBO);
        Registry.register(ModRegistries.CLASS_PERKS, KILL_STREAK.typeId, KILL_STREAK);
    }
}
