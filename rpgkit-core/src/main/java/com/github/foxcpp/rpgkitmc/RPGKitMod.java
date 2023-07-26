package com.github.foxcpp.rpgkitmc;

import com.github.foxcpp.rpgkitmc.classes.ServerButtonHandler;
import com.github.foxcpp.rpgkitmc.classes.abilities.ModAbilities;
import com.github.foxcpp.rpgkitmc.classes.listener.AdvancementsListener;
import com.github.foxcpp.rpgkitmc.classes.listener.ClassReloadListener;
import com.github.foxcpp.rpgkitmc.classes.perks.ModPerks;
import com.github.foxcpp.rpgkitmc.commands.AbilityArgument;
import com.github.foxcpp.rpgkitmc.commands.CharacterClassArgument;
import com.github.foxcpp.rpgkitmc.commands.ModCommands;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RPGKitMod implements ModInitializer {
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final String MOD_ID = "rpgkit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ServerButtonHandler SERVER_BUTTON_HANDLER = new ServerButtonHandler();

    public static final ServerDataSyncer DATA_SYNCER = new ServerDataSyncer();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(ModCommands::register);
        TrackedHandlers.register();
        DATA_SYNCER.setupServer();

        // Classes
        ArgumentTypeRegistry.registerArgumentType(new Identifier(RPGKitMod.MOD_ID, "class"), CharacterClassArgument.class,
                ConstantArgumentSerializer.of(CharacterClassArgument::characterClass));
        ArgumentTypeRegistry.registerArgumentType(new Identifier(RPGKitMod.MOD_ID, "ability"), AbilityArgument.class,
                ConstantArgumentSerializer.of(AbilityArgument::ability));
        ModAbilities.register();
        ModPerks.register();
        DATA_SYNCER.registerListener(new ClassReloadListener());
        DATA_SYNCER.registerListener(new AdvancementsListener());

        SERVER_BUTTON_HANDLER.register();

        LOGGER.info("RPGKit Core loaded");
    }
}
