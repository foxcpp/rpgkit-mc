package com.github.sweetsnowywitch.csmprpgkit.commands;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.classes.Ability;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class AbilityArgument implements ArgumentType<Ability> {
    @Contract(value = " -> new", pure = true)
    public static @NotNull AbilityArgument ability() {
        return new AbilityArgument();
    }

    public static <S> Ability getAbility(String name, @NotNull CommandContext<S> context) {
        return context.getArgument(name, Ability.class);
    }

    @Override
    public Ability parse(StringReader reader) throws CommandSyntaxException {
        var id = Identifier.fromCommandInput(reader);
        var ability = ModRegistries.ABILITIES.get(id);
        if (ability == null) {
            throw new SimpleCommandExceptionType(Text.literal("unknown ability")).createWithContext(reader);
        }
        return ability;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource ?
                CommandSource.suggestMatching(ModRegistries.ABILITIES.getIds().stream().map(Identifier::toString), builder) :
                Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return ModRegistries.ABILITIES.getIds().stream().map(Identifier::toString).toList();
    }
}
