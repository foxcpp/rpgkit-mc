package com.github.sweetsnowywitch.csmprpgkit;

import com.github.sweetsnowywitch.csmprpgkit.magic.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ModRegistries {
    // fixed
    public static final Registry<SpellEffect> SPELL_EFFECTS = FabricRegistryBuilder.createSimple(SpellEffect.class,
            new Identifier(RPGKitMod.MOD_ID, "spell_effects")).buildAndRegister();
    public static final Registry<SpellForm> SPELL_FORMS = FabricRegistryBuilder.createSimple(SpellForm.class,
            new Identifier(RPGKitMod.MOD_ID, "spell_forms")).buildAndRegister();

    // loaded from data packs
    public static final BiMap<Identifier, Aspect> ASPECTS = HashBiMap.create();
    public static final BiMap<Identifier, Spell> SPELLS = HashBiMap.create();
    public static final Map<Identifier, SpellReaction> REACTIONS = new HashMap<>();
    public static final SpellRecipeMap<SpellReaction> REACTION_RECIPES = new SpellRecipeMap<>();
    public static final SpellRecipeMap<Spell> SPELL_RECIPES = new SpellRecipeMap<>();
    public static final Map<Identifier, Map<String, Float>> ITEM_COSTS = new HashMap<>();
}
