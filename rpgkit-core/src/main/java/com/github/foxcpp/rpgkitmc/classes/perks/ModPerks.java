package com.github.foxcpp.rpgkitmc.classes.perks;

import com.github.foxcpp.rpgkitmc.ModRegistries;
import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.classes.Perk;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModPerks {
    public static final Perk STATUS_EFFECT = new StatusEffectPerk(Identifier.of(RPGKitMod.MOD_ID, "status_effect"));
    public static final ComboPerk COMBO = new ComboPerk(Identifier.of(RPGKitMod.MOD_ID, "combo"));
    public static final KillStreakPerk KILL_STREAK = new KillStreakPerk(Identifier.of(RPGKitMod.MOD_ID, "kill_streak"));
    public static final SingleKillPerk SINGLE_KILL = new SingleKillPerk(Identifier.of(RPGKitMod.MOD_ID, "single_kill"));
    public static final RestPerk REST = new RestPerk(Identifier.of(RPGKitMod.MOD_ID, "rest"));
    public static final AttributePerk ATTRIBUTE_PERK = new AttributePerk(Identifier.of(RPGKitMod.MOD_ID, "attribute_perk"));

    public static void register() {
        Registry.register(ModRegistries.CLASS_PERKS, STATUS_EFFECT.typeId, STATUS_EFFECT);
        Registry.register(ModRegistries.CLASS_PERKS, COMBO.typeId, COMBO);
        Registry.register(ModRegistries.CLASS_PERKS, KILL_STREAK.typeId, KILL_STREAK);
        Registry.register(ModRegistries.CLASS_PERKS, SINGLE_KILL.typeId, SINGLE_KILL);
        Registry.register(ModRegistries.CLASS_PERKS, REST.typeId, REST);
        Registry.register(ModRegistries.CLASS_PERKS, ATTRIBUTE_PERK.typeId, ATTRIBUTE_PERK);
    }
}
