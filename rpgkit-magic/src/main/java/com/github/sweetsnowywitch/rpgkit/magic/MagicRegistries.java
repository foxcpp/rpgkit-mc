package com.github.sweetsnowywitch.rpgkit.magic;

import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.ItemEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.Aspect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellForm;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellRecipeMap;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class MagicRegistries {
    // fixed
    public static final Registry<ItemEffect.JsonFactory> ITEM_EFFECTS = FabricRegistryBuilder.createSimple(ItemEffect.JsonFactory.class,
            new Identifier(RPGKitMagicMod.MOD_ID, "item_effects")).buildAndRegister();
    public static final Registry<AreaEffect.JsonFactory> AREA_EFFECTS = FabricRegistryBuilder.createSimple(AreaEffect.JsonFactory.class,
            new Identifier(RPGKitMagicMod.MOD_ID, "area_effects")).buildAndRegister();
    public static final Registry<UseEffect.JsonFactory> USE_EFFECTS = FabricRegistryBuilder.createSimple(UseEffect.JsonFactory.class,
            new Identifier(RPGKitMagicMod.MOD_ID, "use_effects")).buildAndRegister();

    public static final Registry<MagicArea.Factory> EFFECT_AREAS = FabricRegistryBuilder.createSimple(MagicArea.Factory.class,
            new Identifier(RPGKitMagicMod.MOD_ID, "areas")).buildAndRegister();
    public static final Registry<SpellReaction.JsonFactory> REACTIONS = FabricRegistryBuilder.createSimple(SpellReaction.JsonFactory.class,
            new Identifier(RPGKitMagicMod.MOD_ID, "reactions")).buildAndRegister();
    public static final Registry<SpellForm> FORMS = FabricRegistryBuilder.createSimple(SpellForm.class,
            new Identifier(RPGKitMagicMod.MOD_ID, "forms")).buildAndRegister();

    // loaded from data packs
    public static final Map<Identifier, Aspect> ASPECTS = new HashMap<>();
    public static final SpellRecipeMap<Aspect> ASPECT_RECIPES = new SpellRecipeMap<>();
    public static final Map<Identifier, Map<String, Float>> ITEM_COSTS = new HashMap<>();
    public static final Map<Identifier, ItemMapping> TRANSMUTE_MAPPINGS = new HashMap<>();
}
