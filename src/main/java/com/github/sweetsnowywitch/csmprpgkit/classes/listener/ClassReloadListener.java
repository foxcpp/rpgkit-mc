package com.github.sweetsnowywitch.csmprpgkit.classes.listener;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.Ability;
import com.github.sweetsnowywitch.csmprpgkit.classes.CharacterClass;
import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

public class ClassReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new Gson();
    public static Map<Identifier, JsonElement> lastLoadedData;

    public ClassReloadListener() {
        super(GSON, "classes/classes");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        load(prepared);
    }

    public static void load(Map<Identifier, JsonElement> prepared) {
        var classes = new HashMap<Identifier, CharacterClass>();

        for (var classEnt : prepared.entrySet()) {
            try {
                var model = classEnt.getValue().getAsJsonObject();

                ImmutableList.Builder<CharacterClass.Level> levels = ImmutableList.builder();
                for (JsonElement levelElement : model.getAsJsonArray("levels")) {
                    var obj = levelElement.getAsJsonObject();

                    var level = obj.get("level").getAsInt();

                    ImmutableList.Builder<Ability> proficiency = ImmutableList.builder();
                    for (JsonElement proficiencyElement : obj.getAsJsonArray("proficiency")) {
                        var abilityID = Identifier.tryParse(proficiencyElement.getAsString());
                        if (abilityID == null) {
                            throw new IllegalArgumentException("malformed ability id: %s".formatted(proficiencyElement.getAsString()));
                        }
                        var ability = ModRegistries.ABILITIES.get(abilityID);
                        if (ability == null) {
                            throw new IllegalArgumentException("unknown ability: %s".formatted(abilityID));
                        }
                        proficiency.add(ability);
                    }

                    ImmutableMap.Builder<Ability, Integer> increase = ImmutableMap.builder();
                    for (var entry : obj.getAsJsonObject("abilities").entrySet()) {
                        var abilityID = Identifier.tryParse(entry.getKey());
                        if (abilityID == null) {
                            throw new IllegalArgumentException("malformed ability id: %s".formatted(entry.getKey()));
                        }
                        var ability = ModRegistries.ABILITIES.get(abilityID);
                        if (ability == null) {
                            throw new IllegalArgumentException("unknown ability: %s".formatted(abilityID));
                        }
                        increase.put(ability, entry.getValue().getAsInt());
                    }

                    ImmutableList.Builder<Perk> perks = ImmutableList.builder();
                    for (var perkElement : obj.getAsJsonArray("perks")) {
                        var perkObj = perkElement.getAsJsonObject();
                        var perkID = new Identifier(perkObj.get("type").getAsString());
                        var perk = ModRegistries.CLASS_PERKS.get(perkID);
                        if (perk == null) {
                            throw new IllegalArgumentException("unknown perk: %s".formatted(perkID.toString()));
                        }
                        perks.add(perk.withParametersFromJSON(perkObj));
                    }

                    var levelData = new CharacterClass.Level(
                            level, proficiency.build(), increase.build(),
                            perks.build());
                    levels.add(levelData);
                    RPGKitMod.LOGGER.debug("Defined level {} for class {} with proficiency in {}, increase in {} and perks {}",
                            level, classEnt.getKey(), levelData.abilitiesProficiency, levelData.abilitiesIncrease,
                            levelData.perks);
                }

                var klass = new CharacterClass(classEnt.getKey(), levels.build());

                RPGKitMod.LOGGER.debug("Loaded class {} with {} level definitions", classEnt.getKey(), klass.levels.size());
                classes.put(classEnt.getKey(), klass);
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading class definition for {}: {}", classEnt.getKey(), e);
            }
        }
        ModRegistries.CLASSES.clear();
        ModRegistries.CLASSES.putAll(classes);
        RPGKitMod.LOGGER.info("Loaded {} class definitions", classes.size());
        lastLoadedData = prepared;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMod.MOD_ID, "classes/classes");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of();
    }
}
