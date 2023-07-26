package com.github.foxcpp.rpgkitmc.classes.listener;

import com.github.foxcpp.rpgkitmc.ModRegistries;
import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.ServerDataSyncer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

public class AdvancementsListener extends JsonDataLoader implements IdentifiableResourceReloadListener, ServerDataSyncer.SyncableListener {
    private static final Gson GSON = new Gson();
    private Map<Identifier, JsonElement> lastLoadedData;

    public AdvancementsListener() {
        super(GSON, "classes/advancements");
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMod.MOD_ID, "advancements");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        this.loadSynced(prepared);
    }

    @Override
    public Map<Identifier, JsonElement> getLastLoadedData() {
        return lastLoadedData;
    }

    @Override
    public void loadSynced(Map<Identifier, JsonElement> prepared) {
        var advancements = new HashMap<Identifier, Integer>();
        //"minecraft:adventure/sleep_in_bed"

        for (var classEnt : prepared.entrySet()) {
            try {
                var model = classEnt.getValue().getAsJsonObject();

                for (JsonElement levelElement : model.getAsJsonArray("advancements")) {
                    var obj = levelElement.getAsJsonObject();
                    var id = Identifier.tryParse(obj.get("id").getAsString());
                    var reward = obj.get("reward").getAsInt();

                    advancements.put(id, reward);
                    RPGKitMod.LOGGER.debug("Defined prize {} for advancement {}", reward, id);
                }
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading advancement definition for {}: {}", classEnt.getKey(), e);
            }
        }
        ModRegistries.ADVANCEMENTS.clear();
        ModRegistries.ADVANCEMENTS.putAll(advancements);
        RPGKitMod.LOGGER.info("Loaded {} advancement definitions", advancements.size());
        this.lastLoadedData = prepared;
    }
}
