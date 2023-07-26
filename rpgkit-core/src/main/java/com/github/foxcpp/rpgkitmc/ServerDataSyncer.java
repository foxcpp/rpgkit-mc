package com.github.foxcpp.rpgkitmc;

import com.github.foxcpp.rpgkitmc.events.DataRegistryReloadCallback;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ServerDataSyncer {
    private static final Identifier SERVER_DATA_SYNC_PACKET_ID = new Identifier(RPGKitMod.MOD_ID, "server_data_sync");

    public interface SyncableListener extends IdentifiableResourceReloadListener {
        Map<Identifier, JsonElement> getLastLoadedData();

        void loadSynced(Map<Identifier, JsonElement> synced);
    }

    private final Map<Identifier, SyncableListener> listeners = new HashMap<>();

    public void setupServer() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> this.sendServerData(server, null));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> this.sendServerData(server, handler.player));
    }

    @Environment(EnvType.CLIENT)
    public void setupClient() {
        ClientPlayNetworking.registerGlobalReceiver(SERVER_DATA_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            var jsonBlob = buf.readString(1024 * 1024);
            client.execute(() -> this.loadServerData(jsonBlob));
        });
    }

    public void registerListener(SyncableListener listener) {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(listener);
        this.listeners.put(listener.getFabricId(), listener);
    }

    private static JsonObject asJsonMap(Map<Identifier, JsonElement> lastLoadedData) {
        var data = new JsonObject();
        for (var entry : lastLoadedData.entrySet()) {
            data.add(entry.getKey().toString(), entry.getValue());
        }
        return data;
    }

    private static HashMap<Identifier, JsonElement> fromJsonMap(JsonObject obj) {
        var res = new HashMap<Identifier, JsonElement>();
        for (var entry : obj.entrySet()) {
            var id = Identifier.tryParse(entry.getKey());
            if (id == null) {
                throw new JsonSyntaxException("Invalid identifier: %s".formatted(entry.getKey()));
            }
            res.put(id, entry.getValue());
        }
        return res;
    }

    public void sendServerData(MinecraftServer server, @Nullable ServerPlayerEntity player) {
        var dataObject = new JsonObject();

        for (var l : this.listeners.values()) {
            var data = l.getLastLoadedData();

            if (data == null) {
                RPGKitMod.LOGGER.warn("Null data returned for listener {}", l.getFabricId());
                data = new HashMap<>();
            }

            dataObject.add(l.getFabricId().toString(), asJsonMap(data));
        }

        var jsonBlob = dataObject.toString();

        if (player != null) {
            var buf = PacketByteBufs.create();
            buf.writeString(jsonBlob, 1024 * 1024);
            ServerPlayNetworking.send(player, SERVER_DATA_SYNC_PACKET_ID, buf);
            RPGKitMod.LOGGER.info("Sent mod registries to {}", player);
        } else {
            for (var playerListed : server.getPlayerManager().getPlayerList()) {
                var buf = PacketByteBufs.create();
                buf.writeString(jsonBlob);
                ServerPlayNetworking.send(playerListed, SERVER_DATA_SYNC_PACKET_ID, buf);
            }
            RPGKitMod.LOGGER.info("Sent mod registries to all players");
        }
    }

    public void loadServerData(String jsonBlob) {
        RPGKitMod.LOGGER.info("Loading server data...");

        HashMap<Identifier, HashMap<Identifier, JsonElement>> syncedData = new HashMap<>();

        try {
            var json = RPGKitMod.GSON.fromJson(jsonBlob, JsonObject.class);

            for (var l : this.listeners.values()) {
                var obj = json.getAsJsonObject(l.getFabricId().toString());

                if (obj == null) {
                    RPGKitMod.LOGGER.warn("Missing server data for listener {}, check mod version", l.getFabricId());
                    continue;
                }

                syncedData.put(l.getFabricId(), fromJsonMap(obj));
            }
        } catch (JsonSyntaxException ex) {
            RPGKitMod.LOGGER.error("Error occurred while decoding JSON data from server: {}", ex.toString());
            return;
        }

        try {
            for (var l : this.listeners.values()) {
                var data = syncedData.get(l.getFabricId());
                if (data == null) {
                    RPGKitMod.LOGGER.warn("Missing server data for listener {}, check mod version", l.getFabricId());
                    continue;
                }

                l.loadSynced(data);
            }
        } catch (Exception ex) {
            RPGKitMod.LOGGER.error("Error occurred while loading JSON data from server, registries may be in a broken state! {}", ex.toString());
        }

        DataRegistryReloadCallback.EVENT.invoker().onReloaded();
    }
}
