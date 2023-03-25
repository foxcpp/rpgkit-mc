package com.github.sweetsnowywitch.csmprpgkit;

import com.github.sweetsnowywitch.csmprpgkit.commands.ModCommands;
import com.github.sweetsnowywitch.csmprpgkit.commands.SpellFormArgument;
import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.ModEffects;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.AspectReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.ReactionReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.SpellReloadListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RPGKitMod implements ModInitializer  {
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final String MOD_ID = "csmprpgkit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier SERVER_DATA_SYNC_PACKET_ID = new Identifier(MOD_ID, "server_data_sync");

    @Override
    public void onInitialize() {
        ArgumentTypeRegistry.registerArgumentType(new Identifier(RPGKitMod.MOD_ID, "spell_form"), SpellFormArgument.class,
                ConstantArgumentSerializer.of(SpellFormArgument::spellForm));

        ModItems.register();
        ModEntities.register();
        CommandRegistrationCallback.EVENT.register(ModCommands::register);

        ModEffects.register();
        ModForms.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new AspectReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SpellReloadListener());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ReactionReloadListener());

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            this.sendServerData(server, null);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            this.sendServerData(server, handler.player);
        });
    }

    public void sendServerData(MinecraftServer server, @Nullable ServerPlayerEntity player) {
        var dataObject = new JsonObject();
        // XXX: Keep model in sync with ClientRPGKitMod.loadServerData.
        var dataAspects = new JsonObject();
        for (var entry : AspectReloadListener.lastLoadedData.entrySet()) {
            dataAspects.add(entry.getKey().toString(), entry.getValue());
        }
        var dataSpells = new JsonObject();
        for (var entry : SpellReloadListener.lastLoadedData.entrySet()) {
            dataSpells.add(entry.getKey().toString(), entry.getValue());
        }
        var dataReactions = new JsonObject();
        for (var entry : ReactionReloadListener.lastLoadedData.entrySet()) {
            dataReactions.add(entry.getKey().toString(), entry.getValue());
        }

        dataObject.add("aspects", dataAspects);
        dataObject.add("spells", dataSpells);
        dataObject.add("reactions", dataReactions);

        var jsonBlob = dataObject.toString();

        if (player != null) {
            var buf = PacketByteBufs.create();
            buf.writeString(jsonBlob);
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
}
