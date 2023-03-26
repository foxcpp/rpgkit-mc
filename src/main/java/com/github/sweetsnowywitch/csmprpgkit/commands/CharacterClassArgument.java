package com.github.sweetsnowywitch.csmprpgkit.commands;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.classes.CharacterClass;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CharacterClassArgument extends IdentifierMapArgument<CharacterClass> {
    public CharacterClassArgument() {
        super(ModRegistries.CLASSES);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull CharacterClassArgument characterClass() {
        return new CharacterClassArgument();
    }

    public static <S> CharacterClass getCharacterClass(String name, @NotNull CommandContext<S> context) {
        return context.getArgument(name, CharacterClass.class);
    }
}
