package com.github.sweetsnowywitch.csmprpgkit;

import com.github.sweetsnowywitch.csmprpgkit.classes.Ability;
import com.github.sweetsnowywitch.csmprpgkit.classes.CharacterClass;
import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.github.sweetsnowywitch.csmprpgkit.magic.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class ModRegistries {
    // fixed
    public static final Registry<SpellEffect> SPELL_EFFECTS = FabricRegistryBuilder.createSimple(SpellEffect.class,
            new Identifier(RPGKitMod.MOD_ID, "spell_effects")).buildAndRegister();
    public static final Registry<SpellForm> SPELL_FORMS = FabricRegistryBuilder.createSimple(SpellForm.class,
            new Identifier(RPGKitMod.MOD_ID, "spell_forms")).buildAndRegister();

    public static final Registry<Perk> CLASS_PERKS = FabricRegistryBuilder.createSimple(Perk.class,
            new Identifier(RPGKitMod.MOD_ID, "class_perks")).buildAndRegister();
    public static final Registry<Ability> ABILITIES = FabricRegistryBuilder.createSimple(Ability.class,
            new Identifier(RPGKitMod.MOD_ID, "abilities")).buildAndRegister();

    // loaded from data packs
    public static final BiMap<Identifier, Aspect> ASPECTS = HashBiMap.create();
    public static final SpellRecipeMap<Aspect> ASPECT_RECIPES = new SpellRecipeMap<>();
    public static final BiMap<Identifier, Spell> SPELLS = HashBiMap.create();
    public static final BiMap<Identifier, SpellReaction> REACTIONS = HashBiMap.create();
    public static final SpellRecipeMap<SpellReaction> REACTION_RECIPES = new SpellRecipeMap<>();
    public static final SpellRecipeMap<Spell> SPELL_RECIPES = new SpellRecipeMap<>();
    public static final Map<Identifier, Map<String, Float>> ITEM_COSTS = new HashMap<>();

    public static final BiMap<Identifier, CharacterClass> CLASSES = HashBiMap.create();
}
