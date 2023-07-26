package com.github.foxcpp.rpgkitmc.classes;

import com.github.foxcpp.rpgkitmc.ModRegistries;
import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.commands.ModCommands;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class ServerButtonHandler {
    public static final Identifier PACKET_ID = new Identifier(RPGKitMod.MOD_ID, "levelup_button");

    public enum Action {
        PRESS_CLASS_BUTTON,
        PRESS_CP_BUTTON
    }

    public void register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, this::onPacket);
    }

    private void onPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var value = buf.readString(25);
        var action = ServerButtonHandler.Action.valueOf(value);
        switch (action) {
            case PRESS_CLASS_BUTTON -> this.onClassButtonPressed(server, player, handler, buf, responseSender);
            case PRESS_CP_BUTTON -> this.onClassPointsButtonPressed(server, player, handler, buf, responseSender);
            default -> RPGKitMod.LOGGER.error("Unknown button action received from {}", player);
        }
    }

    private void onClassButtonPressed(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var key = buf.readIdentifier();
        if (ModRegistries.CLASSES.containsKey(key)) {
            try {
                ModCommands.classLevelUp(server.getCommandSource(), player, ModRegistries.CLASSES.get(key));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void onClassPointsButtonPressed(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        var amount = buf.readInt();
        var players = new ArrayList<ServerPlayerEntity>();
        players.add(player);
        try {
            ModCommands.expAdd(server.getCommandSource(), players, amount);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
