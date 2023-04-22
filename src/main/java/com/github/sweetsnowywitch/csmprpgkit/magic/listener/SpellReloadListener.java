package com.github.sweetsnowywitch.csmprpgkit.magic.listener;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.GenericSpell;
import com.github.sweetsnowywitch.csmprpgkit.magic.Spell;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellRecipeMap;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static Gson GSON = new Gson();
    public static Map<Identifier, JsonElement> lastLoadedData;

    public SpellReloadListener() {
        super(GSON, "magic/spells");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        load(prepared);
    }

    public static void load(Map<Identifier, JsonElement> prepared) {
        var spells = new HashMap<Identifier, Spell>();
        var spellRecipes = new SpellRecipeMap<Spell>();

        for (var ent : prepared.entrySet()) {
            try {
                var model = ent.getValue().getAsJsonObject();

                ImmutableList.Builder<SpellEffect> effects = ImmutableList.builder();
                for (JsonElement effectElement : model.getAsJsonArray("effects")) {
                    var obj = effectElement.getAsJsonObject();

                    var effectId = new Identifier(obj.get("type").getAsString());
                    var effect = ModRegistries.SPELL_EFFECTS.get(effectId);
                    if (effect == null) {
                        throw new IllegalArgumentException("unknown effect: %s".formatted(effectId.toString()));
                    }
                    effects.add(effect.withParametersFromJSON(obj));
                }

                ImmutableList.Builder<SpellRecipeMap.Element> recipe = ImmutableList.builder();
                for (JsonElement element : model.getAsJsonArray("recipe")) {
                    var obj = element.getAsJsonObject();
                    recipe.add(SpellRecipeMap.Element.fromJson(obj));
                }

                var spell = new Spell(ent.getKey(), effects.build());

                RPGKitMod.LOGGER.debug("Loaded spell {} with effects={}", ent.getKey(), spell.getEffects());
                spells.put(ent.getKey(), spell);
                spellRecipes.addRecipe(recipe.build(), spell);
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading spell definition for {}: {}", ent.getKey(), e);
            }
        }
        ModRegistries.SPELLS.clear();
        ModRegistries.SPELLS.put(GenericSpell.EMPTY.id, GenericSpell.EMPTY);
        ModRegistries.SPELLS.put(Spell.EMPTY.id, Spell.EMPTY);
        ModRegistries.SPELLS.putAll(spells);
        ModRegistries.SPELL_RECIPES.clear();
        ModRegistries.SPELL_RECIPES.copyFrom(spellRecipes);
        RPGKitMod.LOGGER.info("Loaded {} spell definitions", spells.size());
        lastLoadedData = prepared;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMod.MOD_ID, "magic/spells");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(new Identifier(RPGKitMod.MOD_ID, "magic/aspects"));
    }
}
