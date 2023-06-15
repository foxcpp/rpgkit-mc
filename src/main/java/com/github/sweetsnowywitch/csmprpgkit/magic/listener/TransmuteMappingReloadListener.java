package com.github.sweetsnowywitch.csmprpgkit.magic.listener;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.ServerDataSyncer;
import com.github.sweetsnowywitch.csmprpgkit.magic.ItemTransmuteMapping;
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
        super(RPGKitMod.GSON, "magic/transmute_mapping");
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

                RPGKitMod.LOGGER.debug("Loaded transmute mapping {}", ent.getKey());
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading transmute mapping {}: {}", ent.getKey(), e);
            }
        }

        ModRegistries.TRANSMUTE_MAPPINGS.clear();
        ModRegistries.TRANSMUTE_MAPPINGS.putAll(mappings);
        RPGKitMod.LOGGER.info("Loaded {} transmute mappings", mappings.size());
        lastLoadedData = prepared;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(RPGKitMod.MOD_ID, "magic/transmute_mapping");
    }
}
