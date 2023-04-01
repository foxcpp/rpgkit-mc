package com.github.sweetsnowywitch.csmprpgkit.magic.listener;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.Aspect;
import com.github.sweetsnowywitch.csmprpgkit.magic.Spell;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    color = model.get("color").getAsInt();
                }

                var costsRes = costs.build();
                aspects.put(ent.getKey(), new Aspect(ent.getKey(), kind, costsRes, color));
                RPGKitMod.LOGGER.debug("Loaded aspect {} with kind={}, costs={}", ent.getKey(), kind, costsRes);
            } catch (Exception e) {
                RPGKitMod.LOGGER.error("Error occurred while loading aspect definition for {}: {}", ent.getKey(), e);
            }
        }

        ModRegistries.ASPECTS.clear();
        ModRegistries.ASPECTS.putAll(aspects);
        RPGKitMod.LOGGER.info("Loaded {} aspect definitions", aspects.size());
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
