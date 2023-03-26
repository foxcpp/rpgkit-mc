package com.github.sweetsnowywitch.csmprpgkit.classes;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;

public class CharacterClass {
    public final Identifier id;
    public static class Level {
        public final int level;
        public final ImmutableList<Ability> abilitiesProficiency;
        public final ImmutableMap<Ability, Integer> abilitiesIncrease;
        public final ImmutableList<Perk> perks;

        public Level(int level, ImmutableList<Ability> abilitiesProficiency, ImmutableMap<Ability, Integer> abilitiesIncrease,
                     ImmutableList<Perk> perks) {
            this.level = level;
            this.abilitiesProficiency = abilitiesProficiency;
            this.abilitiesIncrease = abilitiesIncrease;
            this.perks = perks;
        }
    }
    public final ImmutableList<Level> levels;

    public CharacterClass(Identifier id, ImmutableList<Level> levels) {
        this.id = id;
        this.levels = levels;
    }

    @Override
    public String toString() {
        return "CharacterClass{" + this.id.toString() + "}";
    }

    public String translationKey() {
        return this.id.toTranslationKey(RPGKitMod.MOD_ID + ".class");
    }
}
