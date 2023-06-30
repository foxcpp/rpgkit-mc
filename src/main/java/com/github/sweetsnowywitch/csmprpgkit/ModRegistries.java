package com.github.sweetsnowywitch.csmprpgkit;

import com.github.sweetsnowywitch.csmprpgkit.classes.Ability;
import com.github.sweetsnowywitch.csmprpgkit.classes.CharacterClass;
import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModRegistries {
    public static final Registry<Perk> CLASS_PERKS = FabricRegistryBuilder.createSimple(Perk.class,
            new Identifier(RPGKitMod.MOD_ID, "class_perks")).buildAndRegister();
    public static final Registry<Ability> ABILITIES = FabricRegistryBuilder.createSimple(Ability.class,
            new Identifier(RPGKitMod.MOD_ID, "abilities")).buildAndRegister();

    public static final BiMap<Identifier, CharacterClass> CLASSES = HashBiMap.create();
    public static final BiMap<Identifier, Integer> ADVANCEMENTS = HashBiMap.create();
}
