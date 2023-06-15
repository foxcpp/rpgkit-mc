package com.github.sweetsnowywitch.csmprpgkit;

import com.github.sweetsnowywitch.csmprpgkit.classes.abilities.ModAbilities;
import com.github.sweetsnowywitch.csmprpgkit.classes.listener.AdvancementsListener;
import com.github.sweetsnowywitch.csmprpgkit.classes.listener.ClassReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.classes.perks.ModPerks;
import com.github.sweetsnowywitch.csmprpgkit.commands.AbilityArgument;
import com.github.sweetsnowywitch.csmprpgkit.commands.CharacterClassArgument;
import com.github.sweetsnowywitch.csmprpgkit.commands.ModCommands;
import com.github.sweetsnowywitch.csmprpgkit.commands.SpellFormArgument;
import com.github.sweetsnowywitch.csmprpgkit.effects.ModStatusEffects;
import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellBuildHandler;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.ModEffects;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.AspectReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.ReactionReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.SpellReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.magic.listener.TransmuteMappingReloadListener;
import com.github.sweetsnowywitch.csmprpgkit.particle.ModParticles;
import com.github.sweetsnowywitch.csmprpgkit.screen.CatalystBagScreenHandler;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RPGKitMod implements ModInitializer {
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random();
    public static final String MOD_ID = "csmprpgkit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ServerSpellBuildHandler SERVER_SPELL_BUILD_HANDLER =
            new ServerSpellBuildHandler();

    public static final ScreenHandlerType<CatalystBagScreenHandler> CATALYST_BAG_SCREEN_HANDLER =
            new ScreenHandlerType<>(CatalystBagScreenHandler::new);

    public static final ServerDataSyncer DATA_SYNCER = new ServerDataSyncer();

    @Override
    public void onInitialize() {
        // Core
        ModItems.register();
        ModEntities.register();
        ModParticles.register();
        CommandRegistrationCallback.EVENT.register(ModCommands::register);
        TrackedHandlers.register();
        ModAttributes.register();
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

        // Magic
        ArgumentTypeRegistry.registerArgumentType(new Identifier(RPGKitMod.MOD_ID, "spell_form"), SpellFormArgument.class,
                ConstantArgumentSerializer.of(SpellFormArgument::spellForm));
        TrackedDataHandlerRegistry.register(SpellCast.TRACKED_HANDLER);
        ModEffects.register();
        ModForms.register();
        ModStatusEffects.register();
        DATA_SYNCER.registerListener(new AspectReloadListener());
        DATA_SYNCER.registerListener(new SpellReloadListener());
        DATA_SYNCER.registerListener(new ReactionReloadListener());
        DATA_SYNCER.registerListener(new TransmuteMappingReloadListener());
        Registry.register(Registry.SCREEN_HANDLER, Identifier.of(RPGKitMod.MOD_ID, "catalyst_bag"), CATALYST_BAG_SCREEN_HANDLER);
        SERVER_SPELL_BUILD_HANDLER.register();
    }
}
