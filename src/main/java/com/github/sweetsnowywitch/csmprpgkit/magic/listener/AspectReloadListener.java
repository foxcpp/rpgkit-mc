package com.github.sweetsnowywitch.csmprpgkit.magic.listener;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.Aspect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellRecipeMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

public class AspectReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static Gson GSON = new Gson();
    public static Map<Identifier, JsonElement> lastLoadedData;

    public AspectReloadListener() {
        super(GSON, "magic/aspects");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        load(prepared);
    }

    public static void load(Map<Identifier, JsonElement> prepared) {
        var aspects = new HashMap<Identifier, Aspect>();

        for (var ent : prepared.entrySet()) {
            try {
                var model = ent.getValue().getAsJsonObject();

                var kind = Aspect.Kind.valueOf(model.get("kind").getAsString().toUpperCase());
                ImmutableMap.Builder<String, Float> costs = ImmutableMap.builder();
                var costsJson = model.getAsJsonObject("costs");
                if (costsJson != null) {
                    for (var element : costsJson.entrySet()) {
                        costs.put(element.getKey(), element.getValue().getAsFloat());
                    }
                }

                int color = Aspect.DEFAULT_COLOR;
                if (model.has("color")) {
                    color = Integer.parseInt(model.get("color").getAsString(), 16);
                }

                int order = 0;
                if (model.has("order")) {
                    order = model.get("order").getAsInt();
                }

                ImmutableList.Builder<SpellEffect> genericEffects = ImmutableList.builder();
                if (model.has("generic_effects")) {
                    for (JsonElement effectElement : model.getAsJsonArray("generic_effects")) {
                        var obj = effectElement.getAsJsonObject();

                        var effectId = new Identifier(obj.get("type").getAsString());
                        var effect = ModRegistries.SPELL_EFFECTS.get(effectId);
                        if (effect == null) {
                            throw new IllegalArgumentException("unknown effect: %s".formatted(effectId.toString()));
                        }
                        genericEffects.add(effect.withParametersFromJSON(obj));
                    }
                }

                ImmutableList.Builder<SpellReaction> genericReactions = ImmutableList.builder();
                if (model.has("generic_reactions")) {
                    for (JsonElement effectElement : model.getAsJsonArray("generic_reactions")) {
                        var obj = effectElement.getAsJsonObject();

                        SpellReaction reaction;
                        if (obj.has("for_effect")) {
                            var id = new Identifier(obj.get("for_effect").getAsString());
                            var effect = ModRegistries.SPELL_EFFECTS.get(id);
                            if (effect == null) {
                                throw new IllegalArgumentException("unknown effect: %s".formatted(id.toString()));
                            }
                            reaction = effect.reactionType(ent.getKey());
                        } else if (obj.has("for_form")) {
                            throw new IllegalArgumentException("for_form is not supported in generic_reactions");
                        } else {
                            throw new IllegalArgumentException("reaction definition must have for_form or for_effect");
                        }
                        if (reaction == null) {
                            throw new IllegalArgumentException("reaction cannot be defined for that form/effect");
                        }
                        reaction = reaction.withParametersFromJSON(obj);
                        genericReactions.add(reaction);
                    }
                }

                var costsRes = costs.build();
                aspects.put(ent.getKey(), new Aspect(ent.getKey(), kind, costsRes, color,
                        !model.has("recipes"), order,
                        genericEffects.build(), genericReactions.build()));
                RPGKitMod.LOGGER.debug("Loaded aspect {} with kind={}, costs={}", ent.getKey(), kind, costsRes);
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading aspect definition for {}: {}", ent.getKey(), e);
            }
        }

        ModRegistries.ASPECTS.clear();
        ModRegistries.ASPECTS.putAll(aspects);
        RPGKitMod.LOGGER.info("Loaded {} aspect definitions", aspects.size());

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
                            var aspect = ModRegistries.ASPECTS.get(aspectID);
                            if (aspect == null) {
                                throw new IllegalArgumentException("Unknown aspect ID %s in recipe of %s".formatted(idObj.getAsString(), ent.getKey()));
                            }
                            recipeBuilder.add(new SpellRecipeMap.Element(aspect, null, false));
                        }
                        var recipe = recipeBuilder.build();
                        if (recipe.size() > 2) {
                            RPGKitMod.LOGGER.error("Aspect {} recipe cannot contain more than 2 other aspects, ignoring", ent.getKey());
                            continue;
                        }
                        recipes.addRecipe(recipeBuilder.build(), ModRegistries.ASPECTS.get(ent.getKey()));
                    }
                }
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading aspect definition for {}: {}", ent.getKey(), e);
            }
        }
        ModRegistries.ASPECT_RECIPES.clear();
        ModRegistries.ASPECT_RECIPES.copyFrom(recipes);

        lastLoadedData = prepared;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMod.MOD_ID, "magic/aspects");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of();
    }
}
