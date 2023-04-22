package com.github.sweetsnowywitch.csmprpgkit.commands;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.CharacterClass;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellBuilder;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.List;

import static com.github.sweetsnowywitch.csmprpgkit.commands.CharacterClassArgument.characterClass;
import static com.github.sweetsnowywitch.csmprpgkit.commands.CharacterClassArgument.getCharacterClass;
import static com.github.sweetsnowywitch.csmprpgkit.commands.SpellFormArgument.spellForm;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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

        var expNode = literal("exp")
                .build();
        var expQueryNode = literal("query")
                .then(argument("target", player())
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(context -> expQuery(context.getSource(), getPlayer(context, "target"))))
                .executes(context -> expQuery(context.getSource(), context.getSource().getPlayerOrThrow()))
                .build();
        var expAddNode = literal("add")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("target", players())
                .then(argument("amount", integer(0))
                .executes(context -> expAdd(
                        context.getSource(), getPlayers(context, "target"),
                        getInteger(context, "amount")
                ))))
                .build();
        var expResetNode = literal("reset")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("targets", players())
                        .executes(context -> expReset(context.getSource(), getPlayers(context, "target"))))
                .executes(context -> expReset(context.getSource(), List.of(context.getSource().getPlayerOrThrow())))
                .build();
        expNode.addChild(expQueryNode);
        expNode.addChild(expAddNode);
        expNode.addChild(expResetNode);

        var classNode = literal("class").build();
        var classQueryNode = literal("query")
                .then(argument("target", player())
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(context -> classQuery(context.getSource(), getPlayer(context, "target"))))
                .executes(context -> classQuery(context.getSource(), context.getSource().getPlayerOrThrow()))
                .build();
        var classLevelUpNode = literal("levelup")
                .then(argument("class", characterClass())
                .then(argument("target", player())
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(context -> classLevelUp(context.getSource(),
                                getPlayer(context, "target"),
                                getCharacterClass("class", context))))
                .executes(context -> classLevelUp(
                        context.getSource(),
                        context.getSource().getPlayerOrThrow(),
                        getCharacterClass("class", context)
                )))
                .build();
        var classReset = literal("reset")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("target", player())
                        .executes(context -> classReset(context.getSource(), getPlayer(context, "target"))))
                .executes(context -> classReset(context.getSource(), context.getSource().getPlayerOrThrow()))
                .build();
        classNode.addChild(classQueryNode);
        classNode.addChild(classLevelUpNode);
        classNode.addChild(classReset);

        dispatcher.getRoot().addChild(rpgkitNode);
        rpgkitNode.addChild(castNode);
        rpgkitNode.addChild(expNode);
        rpgkitNode.addChild(classNode);
    }

    public static int expQuery(ServerCommandSource source, PlayerEntity entity) throws CommandSyntaxException {
        final var classes = entity.getComponent(ModComponents.CLASS);

        source.sendFeedback(Text.translatable("command.csmprpgkit.exp.query",
                classes.getCurrentLevel(), classes.getUndistributedLevels(), classes.getCurrentLevelExp()), false);

        return classes.getCurrentLevel();
    }

    public static int expAdd(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) throws CommandSyntaxException {
        for (var target : targets) {
            target.getComponent(ModComponents.CLASS).addExp(amount);
        }

        if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.csmprpgkit.exp.add.success_single",
                    amount, targets.stream().iterator().next().getName()
            ), true);
        } else {
            source.sendFeedback(Text.translatable("commands.csmprpgkit.exp.add.success_multiple",
                    amount, targets.size()
            ), true);
        }

        return targets.size();
    }

    public static int expReset(ServerCommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        for (var target : targets) {
            target.getComponent(ModComponents.CLASS).resetExp();
        }

        if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.csmprpgkit.exp.reset.success_single",
                    targets.stream().iterator().next().getName()
            ), true);
        } else {
            source.sendFeedback(Text.translatable("commands.csmprpgkit.exp.reset.success_multiple",
                    targets.size()
            ), true);
        }

        return targets.size();
    }

    public static int classQuery(ServerCommandSource source, PlayerEntity target) throws CommandSyntaxException {
        final var classes = target.getComponent(ModComponents.CLASS);
        source.sendFeedback(Text.translatable("commands.csmprpgkit.class.list"), false);
        for (var klass : classes.classes()) {
            var line = Text.literal(" - ");
            line.append(Text.translatable(klass.translationKey()));
            line.append(" (%d)".formatted(classes.getClassLevel(klass.id)));
            source.sendFeedback(line, false);
        }

        return 1;
    }

    public static int classLevelUp(ServerCommandSource source, PlayerEntity target, CharacterClass characterClass) throws CommandSyntaxException {
        final var classes = target.getComponent(ModComponents.CLASS);

        try {
            classes.levelUp(characterClass.id);

            var line = target.getDisplayName().copy();
            line.append(" ");
            line.append(Text.translatable(characterClass.translationKey()));
            line.append(Text.translatable("commands.csmprpgkit.class.levelup.success", classes.getClassLevel(characterClass.id)));
            source.sendFeedback(line, true);
            return classes.getClassLevel(characterClass.id);
        } catch (IllegalStateException ex) {
            source.sendFeedback(Text.translatable("commands.csmprpgkit.class.levelup.not_enough_levels"), false);
            return 0;
        }
    }

    public static int classReset(ServerCommandSource source, PlayerEntity target) throws CommandSyntaxException {
        final var classes = target.getComponent(ModComponents.CLASS);

        classes.resetClasses();
        source.sendFeedback(Text.translatable("commands.csmprpgkit.class.reset.success", target.getDisplayName()), true);
        return 1;
    }

    public static int cast(ServerCommandSource source, SpellForm form, String elements) throws CommandSyntaxException {
        final String[] parts = elements.split(",");
        final PlayerEntity player = source.getPlayerOrThrow();

        final var builder = new SpellBuilder(5);

        for (String part : parts) {
            Identifier id;
            try {
                id = Identifier.tryParse(part);
            } catch (InvalidIdentifierException ex) {
                throw new SimpleCommandExceptionType(Text.translatable("commands.csmprpgkit.unknown_spell_element")).create();
            }

            var asp = ModRegistries.ASPECTS.get(id);
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

        builder.toServerCast(player, form).perform(source.getWorld());

        return 1;
    }
}
