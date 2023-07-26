package com.github.foxcpp.rpgkitmc.magic.commands;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.events.SubcommandRegisterCallback;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuilder;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellElement;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellForm;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands implements SubcommandRegisterCallback {
    @Override
    public void onRegister(LiteralCommandNode<ServerCommandSource> root, CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cra, CommandManager.RegistrationEnvironment env) {
        var castNode = literal("cast")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("form", SpellFormArgument.spellForm())
                        .then(argument("elements", string())
                                .executes(context -> cast(
                                        context.getSource(),
                                        SpellFormArgument.getSpellForm("form", context),
                                        context.getArgument("elements", String.class)))))
                .build();

        root.addChild(castNode);
    }

    public static int cast(ServerCommandSource source, SpellForm form, String elements) throws CommandSyntaxException {
        final String[] parts = elements.split(",");
        final PlayerEntity player = source.getPlayerOrThrow();

        final var builder = new SpellBuilder(player, 5);

        for (String part : parts) {
            Identifier id;
            try {
                id = Identifier.tryParse(part);
            } catch (InvalidIdentifierException ex) {
                throw new SimpleCommandExceptionType(Text.translatable("commands.rpgkit.unknown_spell_element")).create();
            }

            var asp = MagicRegistries.ASPECTS.get(id);
            if (asp != null) {
                builder.addElement(SpellElement.of(asp));
            } else {
                var item = Registry.ITEM.get(id);
                if (!item.equals(Registry.ITEM.get(Registry.ITEM.getDefaultId()))) {
                    builder.addElement(SpellElement.of(item));
                } else {
                    throw new SimpleCommandExceptionType(Text.literal("Unknown element identifier (no such aspect or item)")).create();
                }
            }
        }

        builder.finishSpell();
        // TODO: Add ability to apply reactions.

        builder.toServerCast(form).perform(source.getWorld());

        return 1;
    }
}
