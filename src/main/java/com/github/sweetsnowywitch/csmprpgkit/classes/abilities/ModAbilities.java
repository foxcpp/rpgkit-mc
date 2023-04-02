package com.github.sweetsnowywitch.csmprpgkit.classes.abilities;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.Ability;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModAbilities {
    // Base abilities.
    public static final Ability STRENGTH = new Ability(Identifier.of(RPGKitMod.MOD_ID, "strength"));
    public static final Ability AGILITY = new Ability(Identifier.of(RPGKitMod.MOD_ID, "agility"));
    public static final Ability CONSTITUTION = new Ability(Identifier.of(RPGKitMod.MOD_ID, "constitution"));
    public static final Ability INTELLIGENCE = new Ability(Identifier.of(RPGKitMod.MOD_ID, "intelligence"));
    public static final Ability WISDOM = new Ability(Identifier.of(RPGKitMod.MOD_ID, "wisdom"));
    public static final Ability CHARISMA = new Ability(Identifier.of(RPGKitMod.MOD_ID, "charisma"));

    // Specific abilities.
    public static final Ability SURVIVAL = new Ability(Identifier.of(RPGKitMod.MOD_ID, "survival"), WISDOM);
    public static final Ability HAND_AGILITY = new Ability(Identifier.of(RPGKitMod.MOD_ID, "hand_agility"), AGILITY);
    public static final Ability MAGIC = new Ability(Identifier.of(RPGKitMod.MOD_ID, "magic"), INTELLIGENCE);

    public static void register() {
        Registry.register(ModRegistries.ABILITIES, STRENGTH.id, STRENGTH);
        Registry.register(ModRegistries.ABILITIES, AGILITY.id, AGILITY);
        Registry.register(ModRegistries.ABILITIES, CONSTITUTION.id, CONSTITUTION);
        Registry.register(ModRegistries.ABILITIES, INTELLIGENCE.id, INTELLIGENCE);
        Registry.register(ModRegistries.ABILITIES, WISDOM.id, WISDOM);
        Registry.register(ModRegistries.ABILITIES, CHARISMA.id, CHARISMA);

        Registry.register(ModRegistries.ABILITIES, SURVIVAL.id, SURVIVAL);
        Registry.register(ModRegistries.ABILITIES, HAND_AGILITY.id, HAND_AGILITY);
        Registry.register(ModRegistries.ABILITIES, MAGIC.id, MAGIC);
    }
}
