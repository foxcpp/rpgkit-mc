package com.github.sweetsnowywitch.csmprpgkit.commands;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellBuilder;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import static com.github.sweetsnowywitch.csmprpgkit.commands.SpellFormArgument.spellForm;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.*;

public class ModCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cra, CommandManager.RegistrationEnvironment env) {
        var rpgkitNode = literal(RPGKitMod.MOD_ID).build();

        var castNode = literal("cast")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("form", spellForm())
                .then(argument("elements", string())
                .executes(context -> cast(
                        context.getSource(),
                        SpellFormArgument.getSpellForm("form", context),
                        context.getArgument("elements", String.class)))))
                .build();

        dispatcher.getRoot().addChild(rpgkitNode);
        rpgkitNode.addChild(castNode);
    }

    public static int cast(ServerCommandSource source, SpellForm form, String elements) throws CommandSyntaxException {
        final String[] parts = elements.split(",");
        final PlayerEntity player = source.getPlayerOrThrow();

        final var builder = new SpellBuilder(form);

        for (String part : parts) {
            Identifier id;
            try {
                id = Identifier.tryParse(part);
            } catch (InvalidIdentifierException ex) {
                throw new SimpleCommandExceptionType(Text.literal("invalid element identifier")).create();
            }

            var asp = ModRegistries.ASPECTS.get(id);
            if (asp != null) {
                builder.addElement(SpellElement.of(asp));
            } else {
                var item = Registries.ITEM.get(id);
                if (!item.equals(Registries.ITEM.get(Registries.ITEM.getDefaultId()))) {
                    builder.addElement(SpellElement.of(item));
                } else {
                    throw new SimpleCommandExceptionType(Text.literal("unknown element identifier (no such aspect or item)")).create();
                }
            }
        }

        builder.complete();
        builder.toCast(player).perform();

        return 1;
    }
}
