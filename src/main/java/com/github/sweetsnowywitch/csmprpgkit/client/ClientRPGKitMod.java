package com.github.sweetsnowywitch.csmprpgkit.client;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.listener.ClassReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.client.overlays.ManaHudOverlay;
import com.github.sweetsnowywitch.csmprpgkit.client.particle.GenericSpellParticle;
import com.github.sweetsnowywitch.csmprpgkit.client.render.ModRenderers;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.AspectReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.ReactionReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.SpellReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.particle.ModParticles;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.screen.PlayerScreenHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.util.Identifier;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class ClientRPGKitMod implements ClientModInitializer {
    public static final Gson GSON = new Gson();

    @Override
    public void onInitializeClient() {
        ModRenderers.register();
        HudRenderCallback.EVENT.register(new ManaHudOverlay());

        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((((atlasTexture, registry) -> {
            registry.register(new Identifier(RPGKitMod.MOD_ID, "particle/generic_spell_0"));
        })));
        ParticleFactoryRegistry.getInstance().register(ModParticles.GENERIC_SPELL, GenericSpellParticle.Factory::new);

        ClientPlayNetworking.registerGlobalReceiver(RPGKitMod.SERVER_DATA_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            var jsonBlob = buf.readString(1024*1024);
            client.execute(() -> this.loadServerData(jsonBlob));
        });
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

    public void loadServerData(String jsonBlob) {
        RPGKitMod.LOGGER.info("Loading server data...");

        HashMap<Identifier, JsonElement> classes;
        HashMap<Identifier, JsonElement> aspects, spells, reactions;

        try {
            var json = GSON.fromJson(jsonBlob, JsonObject.class);

            classes = fromJsonMap(json.getAsJsonObject("classes"));

            aspects = fromJsonMap(json.getAsJsonObject("aspects"));
            spells = fromJsonMap(json.getAsJsonObject("spells"));
            reactions = fromJsonMap(json.getAsJsonObject("reactions"));
        } catch (JsonSyntaxException ex) {
            RPGKitMod.LOGGER.error("Error occurred while decoding JSON data from server: {}", ex.toString());
            return;
        }

        try {
            ClassReloadListener.load(classes);

            AspectReloadListener.load(aspects);
            SpellReloadListener.load(spells);
            ReactionReloadListener.load(reactions);
        } catch (Exception ex) {
            RPGKitMod.LOGGER.error("Error occurred while loading JSON data from server, registries may be in a broken state! {}", ex.toString());
        }
    }
}
