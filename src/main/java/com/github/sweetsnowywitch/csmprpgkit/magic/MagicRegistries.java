package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.ItemEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.UseEffect;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class MagicRegistries {
    // fixed
    public static final Registry<ItemEffect.JsonFactory> ITEM_EFFECTS = FabricRegistryBuilder.createSimple(ItemEffect.JsonFactory.class,
            new Identifier(RPGKitMod.MOD_ID, "magic_item_effects")).buildAndRegister();
    public static final Registry<AreaEffect.JsonFactory> AREA_EFFECTS = FabricRegistryBuilder.createSimple(AreaEffect.JsonFactory.class,
            new Identifier(RPGKitMod.MOD_ID, "magic_area_effects")).buildAndRegister();
    public static final Registry<UseEffect.JsonFactory> USE_EFFECTS = FabricRegistryBuilder.createSimple(UseEffect.JsonFactory.class,
            new Identifier(RPGKitMod.MOD_ID, "magic_use_effects")).buildAndRegister();

    public static final Registry<MagicArea.Factory> EFFECT_AREAS = FabricRegistryBuilder.createSimple(MagicArea.Factory.class,
            new Identifier(RPGKitMod.MOD_ID, "magic_areas")).buildAndRegister();
    public static final Registry<SpellReaction.JsonFactory> REACTIONS = FabricRegistryBuilder.createSimple(SpellReaction.JsonFactory.class,
            new Identifier(RPGKitMod.MOD_ID, "magic_reactions")).buildAndRegister();
    public static final Registry<SpellForm> FORMS = FabricRegistryBuilder.createSimple(SpellForm.class,
            new Identifier(RPGKitMod.MOD_ID, "magic_forms")).buildAndRegister();

    // loaded from data packs
    public static final Map<Identifier, Aspect> ASPECTS = new HashMap<>();
    public static final SpellRecipeMap<Aspect> ASPECT_RECIPES = new SpellRecipeMap<>();
    public static final Map<Identifier, Map<String, Float>> ITEM_COSTS = new HashMap<>();
    public static final Map<Identifier, ItemTransmuteMapping> TRANSMUTE_MAPPINGS = new HashMap<>();
}
