package com.github.foxcpp.rpgkitmc.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public interface SubcommandRegisterCallback {
    Event<SubcommandRegisterCallback> EVENT = EventFactory.createArrayBacked(SubcommandRegisterCallback.class,
            (listeners) -> (LiteralCommandNode<ServerCommandSource> root, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cra, CommandManager.RegistrationEnvironment env) -> {
                for (SubcommandRegisterCallback listener : listeners) {
                    listener.onRegister(root, dispatcher, cra, env);
                }
            });

    void onRegister(LiteralCommandNode<ServerCommandSource> root, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cra, CommandManager.RegistrationEnvironment env);
}
