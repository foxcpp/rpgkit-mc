package com.github.sweetsnowywitch.rpgkit.magic.listener;

import com.github.sweetsnowywitch.rpgkit.ServerDataSyncer;
import com.github.sweetsnowywitch.rpgkit.magic.ItemTransmuteMapping;
import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

public class TransmuteMappingReloadListener extends JsonDataLoader implements IdentifiableResourceReloadListener, ServerDataSyncer.SyncableListener {
    private Map<Identifier, JsonElement> lastLoadedData;

    public TransmuteMappingReloadListener() {
        super(RPGKitMagicMod.GSON, "magic/transmute_mapping");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        loadSynced(prepared);
    }

    @Override
    public Map<Identifier, JsonElement> getLastLoadedData() {
        return lastLoadedData;
    }

    public void loadSynced(Map<Identifier, JsonElement> prepared) {
        var mappings = new HashMap<Identifier, ItemTransmuteMapping>();

        for (var ent : prepared.entrySet()) {
            try {
                var model = ent.getValue().getAsJsonObject();

                mappings.put(ent.getKey(), ItemTransmuteMapping.ofItemStack(ent.getKey(), model));

                RPGKitMagicMod.LOGGER.debug("Loaded transmute mapping {}", ent.getKey());
            } catch (Exception e) {
                RPGKitMagicMod.LOGGER.error("Error occurred while loading transmute mapping {}: {}", ent.getKey(), e);
            }
        }

        MagicRegistries.TRANSMUTE_MAPPINGS.clear();
        MagicRegistries.TRANSMUTE_MAPPINGS.putAll(mappings);
        RPGKitMagicMod.LOGGER.info("Loaded {} transmute mappings", mappings.size());
        lastLoadedData = prepared;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMagicMod.MOD_ID, "magic/transmute_mapping");
    }
}
