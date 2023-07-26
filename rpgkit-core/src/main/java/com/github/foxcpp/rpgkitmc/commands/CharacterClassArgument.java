package com.github.foxcpp.rpgkitmc.commands;

import com.github.foxcpp.rpgkitmc.ModRegistries;
import com.github.foxcpp.rpgkitmc.classes.CharacterClass;
import com.mojang.brigadier.context.CommandContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
