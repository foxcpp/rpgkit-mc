package com.github.foxcpp.rpgkitmc.commands;

import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.components.ModComponents;
import com.github.foxcpp.rpgkitmc.classes.CharacterClass;
import com.github.foxcpp.rpgkitmc.events.SubcommandRegisterCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess cra, CommandManager.RegistrationEnvironment env) {
        var rpgkitNode = literal(RPGKitMod.MOD_ID).build();

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
                .then(argument("class", CharacterClassArgument.characterClass())
                        .then(argument("target", player())
                                .requires(src -> src.hasPermissionLevel(2))
                                .executes(context -> classLevelUp(context.getSource(),
                                        getPlayer(context, "target"),
                                        CharacterClassArgument.getCharacterClass("class", context))))
                        .executes(context -> classLevelUp(
                                context.getSource(),
                                context.getSource().getPlayerOrThrow(),
                                CharacterClassArgument.getCharacterClass("class", context)
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
        rpgkitNode.addChild(expNode);
        rpgkitNode.addChild(classNode);

        SubcommandRegisterCallback.EVENT.invoker().onRegister(rpgkitNode, dispatcher, cra, env);
    }

    public static int expQuery(ServerCommandSource source, PlayerEntity entity) throws CommandSyntaxException {
        final var classes = entity.getComponent(ModComponents.CLASS);

        source.sendFeedback(Text.translatable("command.rpgkit.exp.query",
                classes.getCurrentLevel(), classes.getUndistributedLevels(), classes.getCurrentLevelExp()), false);

        return classes.getCurrentLevel();
    }

    public static int expAdd(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) throws CommandSyntaxException {
        for (var target : targets) {
            target.getComponent(ModComponents.CLASS).addExp(amount);
        }

        if (targets.size() == 1) {
            source.sendFeedback(Text.translatable("commands.rpgkit.exp.add.success_single",
                    amount, targets.stream().iterator().next().getName()
            ), true);
        } else {
            source.sendFeedback(Text.translatable("commands.rpgkit.exp.add.success_multiple",
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
            source.sendFeedback(Text.translatable("commands.rpgkit.exp.reset.success_single",
                    targets.stream().iterator().next().getName()
            ), true);
        } else {
            source.sendFeedback(Text.translatable("commands.rpgkit.exp.reset.success_multiple",
                    targets.size()
            ), true);
        }

        return targets.size();
    }

    public static int classQuery(ServerCommandSource source, PlayerEntity target) throws CommandSyntaxException {
        final var classes = target.getComponent(ModComponents.CLASS);
        source.sendFeedback(Text.translatable("commands.rpgkit.class.list"), false);
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
            line.append(Text.translatable("commands.rpgkit.class.levelup.success", classes.getClassLevel(characterClass.id)));
            source.sendFeedback(line, true);
            return classes.getClassLevel(characterClass.id);
        } catch (IllegalStateException ex) {
            source.sendFeedback(Text.translatable("commands.rpgkit.class.levelup.not_enough_levels"), false);
            return 0;
        }
    }

    public static int classReset(ServerCommandSource source, PlayerEntity target) throws CommandSyntaxException {
        final var classes = target.getComponent(ModComponents.CLASS);

        classes.resetClasses();
        source.sendFeedback(Text.translatable("commands.rpgkit.class.reset.success", target.getDisplayName()), true);
        return 1;
    }
}
