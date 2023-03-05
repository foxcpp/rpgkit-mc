package com.github.sweetsnowywitch.csmprpgkit.client;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.client.render.ModRenderers;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.AspectReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.ReactionReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.SpellReloadListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

@Environment(EnvType.CLIENT)
public class ClientRPGKitMod implements ClientModInitializer {
    public static final Gson GSON = new Gson();

    @Override
    public void onInitializeClient() {
        ModRenderers.register();
        HudRenderCallback.EVENT.register(new ManaHudOverlay());

        ClientPlayNetworking.registerGlobalReceiver(RPGKitMod.SERVER_DATA_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            var jsonBlob = buf.readString(1024*1024);
            client.execute(() -> {
                this.loadServerData(jsonBlob);
            });
        });
    }

    public void loadServerData(String jsonBlob) {
        RPGKitMod.LOGGER.info("Loading server data...");

        var aspects = new HashMap<Identifier, JsonElement>();
        var spells = new HashMap<Identifier, JsonElement>();
        var reactions = new HashMap<Identifier, JsonElement>();

        try {
            var json = GSON.fromJson(jsonBlob, JsonObject.class);

            for (var entry : json.getAsJsonObject("aspects").entrySet()) {
                var id = Identifier.tryParse(entry.getKey());
                if (id == null) {
                    throw new JsonSyntaxException("Invalid identifier: %s".formatted(entry.getKey()));
                }
                aspects.put(id, entry.getValue());
            }
            for (var entry : json.getAsJsonObject("spells").entrySet()) {
                var id = Identifier.tryParse(entry.getKey());
                if (id == null) {
                    throw new JsonSyntaxException("Invalid identifier: %s".formatted(entry.getKey()));
                }
                spells.put(id, entry.getValue());
            }
            for (var entry : json.getAsJsonObject("reactions").entrySet()) {
                var id = Identifier.tryParse(entry.getKey());
                if (id == null) {
                    throw new JsonSyntaxException("Invalid identifier: %s".formatted(entry.getKey()));
                }
                reactions.put(id, entry.getValue());
            }
        } catch (JsonSyntaxException ex) {
            RPGKitMod.LOGGER.error("Error occurred while decoding JSON data from server: {}", ex.toString());
            return;
        }

        try {
            AspectReloadListener.load(aspects);
            SpellReloadListener.load(spells);
            ReactionReloadListener.load(reactions);
        } catch (Exception ex) {
            RPGKitMod.LOGGER.error("Error occurred while loading JSON data from server, registries may be in a broken state! {}", ex.toString());
        }
    }
}
