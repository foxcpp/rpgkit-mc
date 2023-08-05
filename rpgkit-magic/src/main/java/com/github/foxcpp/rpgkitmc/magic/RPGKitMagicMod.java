package com.github.foxcpp.rpgkitmc.magic;

import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.events.SubcommandRegisterCallback;
import com.github.foxcpp.rpgkitmc.magic.blocks.ModBlocks;
import com.github.foxcpp.rpgkitmc.magic.commands.ModCommands;
import com.github.foxcpp.rpgkitmc.magic.commands.SpellFormArgument;
import com.github.foxcpp.rpgkitmc.magic.effects.ModEffects;
import com.github.foxcpp.rpgkitmc.magic.entities.ModEntities;
import com.github.foxcpp.rpgkitmc.magic.form.ModForms;
import com.github.foxcpp.rpgkitmc.magic.items.ModItems;
import com.github.foxcpp.rpgkitmc.magic.listener.AspectReloadListener;
import com.github.foxcpp.rpgkitmc.magic.listener.TransmuteMappingReloadListener;
import com.github.foxcpp.rpgkitmc.magic.particle.ModParticles;
import com.github.foxcpp.rpgkitmc.magic.screen.CatalystBagScreenHandler;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellBuildHandler;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellCast;
import com.github.foxcpp.rpgkitmc.magic.statuseffects.ModStatusEffects;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RPGKitMagicMod implements ModInitializer {
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final String MOD_ID = "rpgkitmagic";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ServerSpellBuildHandler SERVER_SPELL_BUILD_HANDLER =
            new ServerSpellBuildHandler();

    public static final ScreenHandlerType<CatalystBagScreenHandler> CATALYST_BAG_SCREEN_HANDLER =
            new ScreenHandlerType<>(CatalystBagScreenHandler::new);

    @Override
    public void onInitialize() {
        ModStatusEffects.register();
        ModEntities.register();
        ModItems.register();
        ModBlocks.register();
        ModParticles.register();
        ModAttributes.register();

        SubcommandRegisterCallback.EVENT.register(new ModCommands());
        RPGKitMod.DATA_SYNCER.registerListener(new AspectReloadListener());
        RPGKitMod.DATA_SYNCER.registerListener(new TransmuteMappingReloadListener());

        ArgumentTypeRegistry.registerArgumentType(new Identifier(MOD_ID, "spell_form"), SpellFormArgument.class,
                ConstantArgumentSerializer.of(SpellFormArgument::spellForm));
        TrackedDataHandlerRegistry.register(SpellCast.TRACKED_HANDLER);
        TrackedDataHandlerRegistry.register(EffectVector.TRACKED_HANDLER);
        TrackedDataHandlerRegistry.register(EffectVector.OPTIONAL_TRACKED_HANDLER);
        ModEffects.register();
        ModForms.register();
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "catalyst_bag"), CATALYST_BAG_SCREEN_HANDLER);
        SERVER_SPELL_BUILD_HANDLER.register();

        LOGGER.info("RPGKit Magic loaded");
    }
}
