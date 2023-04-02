package com.github.sweetsnowywitch.csmprpgkit.commands;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
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

public class SpellFormArgument implements ArgumentType<SpellForm> {
    @Contract(value = " -> new", pure = true)
    public static @NotNull SpellFormArgument spellForm() {
        return new SpellFormArgument();
    }

    public static <S> SpellForm getSpellForm(String name, @NotNull CommandContext<S> context) {
        return context.getArgument(name, SpellForm.class);
    }

    @Override
    public SpellForm parse(StringReader reader) throws CommandSyntaxException {
        var id = Identifier.fromCommandInput(reader);
        var form = ModRegistries.SPELL_FORMS.get(id);
        if (form == null) {
            throw new SimpleCommandExceptionType(Text.literal("unknown spell form")).createWithContext(reader);
        }
        return form;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof CommandSource ?
                CommandSource.suggestMatching(ModRegistries.SPELL_FORMS.getIds().stream().map(Identifier::toString), builder) :
                Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return ModRegistries.SPELL_FORMS.getIds().stream().map(Identifier::toString).toList();
    }
}
