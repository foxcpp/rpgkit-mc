package com.github.foxcpp.rpgkitmc.magic.listener;

import com.github.foxcpp.rpgkitmc.ServerDataSyncer;
import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.spell.Aspect;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellRecipeMap;
import com.google.common.collect.ImmutableList;
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

public class AspectReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener, ServerDataSyncer.SyncableListener {
    private Map<Identifier, JsonElement> lastLoadedData;

    public AspectReloadListener() {
        super(RPGKitMagicMod.GSON, "magic/aspects");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        loadSynced(prepared);
    }

    public void loadSynced(Map<Identifier, JsonElement> prepared) {
        var aspects = new HashMap<Identifier, Aspect>();

        // First load aspects as empty objects so any inter-references in aspect definitions will actually
        // work correctly. They will reference empty objects but it is fine since equality comparison is based
        // on identifiers.
        for (var ent : prepared.entrySet()) {
            try {
                var entObj = ent.getValue().getAsJsonObject();
                aspects.put(ent.getKey(), new Aspect(ent.getKey(),
                        !entObj.has("recipes"),
                        Aspect.Kind.valueOf(entObj.get("kind").getAsString().toUpperCase())));
                RPGKitMagicMod.LOGGER.debug("Registered aspect {}", ent.getKey());
            } catch (Exception e) {
                RPGKitMagicMod.LOGGER.error("Error occurred while loading aspect definition for {}: {}", ent.getKey(), e);
            }
        }
        MagicRegistries.ASPECTS.clear();
        MagicRegistries.ASPECTS.putAll(aspects);
        RPGKitMagicMod.LOGGER.debug("Pre-loaded {} aspect definitions", aspects.size());

        for (var ent : prepared.entrySet()) {
            try {
                var entObj = ent.getValue().getAsJsonObject();
                aspects.put(ent.getKey(), new Aspect(ent.getKey(), !entObj.has("recipes"), entObj));
                RPGKitMagicMod.LOGGER.debug("Loaded aspect {}", ent.getKey());
            } catch (Exception e) {
                RPGKitMagicMod.LOGGER.error("Error occurred while loading aspect definition for {}: {}", ent.getKey(), e);
            }
        }

        MagicRegistries.ASPECTS.clear();
        MagicRegistries.ASPECTS.putAll(aspects);
        RPGKitMagicMod.LOGGER.info("Loaded {} aspect definitions", aspects.size());

        var recipes = new SpellRecipeMap<Aspect>();
        for (var ent : prepared.entrySet()) {
            try {
                var model = ent.getValue().getAsJsonObject();
                if (model.has("recipes")) {
                    for (JsonElement recipeElement : model.getAsJsonArray("recipes")) {
                        ImmutableList.Builder<SpellRecipeMap.Element> recipeBuilder = ImmutableList.builder();
                        for (var idObj : recipeElement.getAsJsonArray()) {
                            var aspectID = Identifier.tryParse(idObj.getAsString());
                            if (aspectID == null) {
                                throw new IllegalArgumentException("Malformed aspect ID: %s".formatted(idObj.getAsString()));
                            }
                            var aspect = MagicRegistries.ASPECTS.get(aspectID);
                            if (aspect == null) {
                                throw new IllegalArgumentException("Unknown aspect ID %s in recipe of %s".formatted(idObj.getAsString(), ent.getKey()));
                            }
                            recipeBuilder.add(new SpellRecipeMap.Element(aspect, null, false));
                        }
                        var recipe = recipeBuilder.build();
                        if (recipe.size() > 2) {
                            RPGKitMagicMod.LOGGER.error("Aspect {} recipe cannot contain more than 2 other aspects, ignoring", ent.getKey());
                            continue;
                        }
                        var asp = MagicRegistries.ASPECTS.get(ent.getKey());
                        if (asp == null) { // If aspect load failed for some reason.
                            continue;
                        }
                        recipes.addRecipe(recipeBuilder.build(), asp);
                    }
                }
            } catch (Exception e) {
                RPGKitMagicMod.LOGGER.error("Error occurred while loading aspect definition for {}: {}", ent.getKey(), e);
            }
        }
        MagicRegistries.ASPECT_RECIPES.clear();
        MagicRegistries.ASPECT_RECIPES.copyFrom(recipes);

        lastLoadedData = prepared;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMagicMod.MOD_ID, "magic/aspects");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(
                new Identifier(RPGKitMagicMod.MOD_ID, "magic/transmute_mapping")
        );
    }

    @Override
    public Map<Identifier, JsonElement> getLastLoadedData() {
        return lastLoadedData;
    }
}
