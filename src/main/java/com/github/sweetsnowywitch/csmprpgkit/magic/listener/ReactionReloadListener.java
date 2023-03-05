package com.github.sweetsnowywitch.csmprpgkit.magic.listener;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.Spell;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
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

public class ReactionReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static Gson GSON = new Gson();
    public static Map<Identifier, JsonElement> lastLoadedData;

    public ReactionReloadListener() {
        super(GSON, "magic/reactions");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        load(prepared);
    }

    public static void load(Map<Identifier, JsonElement> prepared) {
        var reactions = new HashMap<Identifier, SpellReaction>();
        var reactionRecipes = new SpellRecipeMap<SpellReaction>();

        for (var ent : prepared.entrySet()) {
            try {
                var model = ent.getValue().getAsJsonObject();

                SpellReaction reaction;
                if (model.has("for_effect")) {
                    var id = new Identifier(model.get("for_effect").getAsString());
                    var effect = ModRegistries.SPELL_EFFECTS.get(id);
                    if (effect == null) {
                        throw new IllegalArgumentException("unknown effect: %s".formatted(id.toString()));
                    }
                    reaction = effect.reactionType();
                } else if (model.has("for_form")) {
                    var id = new Identifier(model.get("for_form").getAsString());
                    var form = ModRegistries.SPELL_FORMS.get(id);
                    if (form == null) {
                        throw new IllegalArgumentException("unknown reaction: %s".formatted(id.toString()));
                    }
                    reaction = form.reactionType();
                } else {
                    throw new IllegalArgumentException("reaction definition must have for_form or for_effect");
                }
                if (reaction == null) {
                    throw new IllegalArgumentException("reaction cannot be defined for that form/effect");
                }

                reaction = reaction.withParametersFromJSON(model);

                ImmutableList<SpellRecipeMap.Element> recipe = ImmutableList.copyOf(
                        model.getAsJsonArray("recipe").asList().stream()
                                .map(el -> SpellRecipeMap.Element.fromJson(el.getAsJsonObject())).iterator());


                reactions.put(ent.getKey(), reaction);
                reactionRecipes.addRecipe(recipe, reaction);
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading reaction definition for {}: {}", ent.getKey(), e);
            }
        }
        ModRegistries.REACTIONS.clear();
        ModRegistries.REACTIONS.putAll(reactions);
        ModRegistries.REACTION_RECIPES.clear();
        ModRegistries.REACTION_RECIPES.copyFrom(reactionRecipes);
        RPGKitMod.LOGGER.info("Loaded {} reaction definitions", reactions.size());
        lastLoadedData = prepared;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMod.MOD_ID, "magic/reactions");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(new Identifier(RPGKitMod.MOD_ID, "magic/aspects"));
    }
}
