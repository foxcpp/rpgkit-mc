package com.github.sweetsnowywitch.csmprpgkit.magic.listener;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.ServerDataSyncer;
import com.github.sweetsnowywitch.csmprpgkit.magic.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AspectReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener, ServerDataSyncer.SyncableListener {
    private Map<Identifier, JsonElement> lastLoadedData;

    public AspectReloadListener() {
        super(RPGKitMod.GSON, "magic/aspects");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        loadSynced(prepared);
    }

    private static String sha256(String text) {
        try {
            var messageDigest = MessageDigest.getInstance("SHA-256");
            var hash = messageDigest.digest(text.getBytes(StandardCharsets.UTF_8));

            return String.format("%064x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadSynced(Map<Identifier, JsonElement> prepared) {
        var aspects = new HashMap<Identifier, Aspect>();
        var genericReactionsMap = new HashMap<Identifier, SpellReaction>();

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
                        genericEffects.add(SpellEffect.fromJson(effectElement.getAsJsonObject()));
                    }
                }

                ImmutableList.Builder<SpellReaction> genericReactions = ImmutableList.builder();
                if (model.has("generic_reactions")) {
                    var i = 0;
                    for (JsonElement effectElement : model.getAsJsonArray("generic_reactions")) {
                        var obj = effectElement.getAsJsonObject();

                        var reactionHash = sha256(RPGKitMod.GSON.toJson(obj));
                        var reactionId = Identifier.of(ent.getKey().getNamespace(), "generic_reaction/" + ent.getKey().getPath() + "/" + reactionHash);

                        SpellReaction reaction;
                        if (obj.has("for_effect")) {
                            var id = new Identifier(obj.get("for_effect").getAsString());
                            var effect = ModRegistries.SPELL_EFFECT_REACTIONS.get(id);
                            if (effect == null) {
                                throw new IllegalArgumentException("unknown effect: %s".formatted(id.toString()));
                            }
                            reaction = effect.createReactionFromJson(reactionId, obj);
                        } else if (obj.has("for_form")) {
                            var id = new Identifier(obj.get("for_form").getAsString());
                            var form = ModRegistries.SPELL_FORM_REACTIONS.get(id);
                            if (form == null) {
                                throw new IllegalArgumentException("unknown form: %s".formatted(id.toString()));
                            }
                            reaction = form.createReactionFromJson(reactionId, obj);
                        } else {
                            throw new IllegalArgumentException("reaction definition must have for_form or for_effect");
                        }
                        if (reaction == null) {
                            throw new IllegalArgumentException("reaction cannot be defined for that form/effect");
                        }
                        genericReactions.add(reaction);
                        genericReactionsMap.put(reactionId, reaction);
                        i++;
                    }
                }

                SpellForm preferredForm = null;
                int preferredFormWeight = 0;
                if (model.has("use_form")) {
                    var formJson = model.getAsJsonObject("use_form");
                    preferredForm = ModRegistries.SPELL_FORMS.get(new Identifier(formJson.get("id").getAsString()));
                    preferredFormWeight = formJson.get("weight").getAsInt();
                }

                var costsRes = costs.build();
                aspects.put(ent.getKey(), new Aspect(ent.getKey(), kind, costsRes, color,
                        !model.has("recipes"), order,
                        genericEffects.build(), genericReactions.build(),
                        preferredForm, preferredFormWeight));
                RPGKitMod.LOGGER.debug("Loaded aspect {} with kind={}, costs={}", ent.getKey(), kind, costsRes);
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading aspect definition for {}: {}", ent.getKey(), e);
            }
        }

        ModRegistries.ASPECTS.clear();
        ModRegistries.REACTIONS.keySet().removeIf(key -> key.getPath().startsWith("generic_reaction"));
        ModRegistries.ASPECTS.putAll(aspects);
        ModRegistries.REACTIONS.putAll(genericReactionsMap);

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
                        var asp = ModRegistries.ASPECTS.get(ent.getKey());
                        if (asp == null) { // If aspect load failed for some reason.
                            continue;
                        }
                        recipes.addRecipe(recipeBuilder.build(), asp);
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

    @Override
    public Map<Identifier, JsonElement> getLastLoadedData() {
        return lastLoadedData;
    }
}
