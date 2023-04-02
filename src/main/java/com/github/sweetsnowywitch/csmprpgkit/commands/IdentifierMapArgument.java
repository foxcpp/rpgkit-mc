package com.github.sweetsnowywitch.csmprpgkit.commands;

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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IdentifierMapArgument<T> implements ArgumentType<T> {
    private final Map<Identifier, T> registry;
    public IdentifierMapArgument(Map<Identifier, T> registry) {
        this.registry = registry;
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        var id = Identifier.fromCommandInput(reader);
        var value = this.registry.get(id);
        if (value == null) {
            throw new SimpleCommandExceptionType(Text.literal("unknown identifier")).createWithContext(reader);
        }
        return value;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource ?
                CommandSource.suggestMatching(this.registry.keySet().stream().map(Identifier::toString), builder) :
                Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return this.registry.keySet().stream().map(Identifier::toString).toList();
    }
}
