package com.github.sweetsnowywitch.csmprpgkit.classes;

import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class Ability {
    public final Identifier id;
    public final Identifier baseId;

    public static int DEFAULT_ABILITY_VALUE = 10;

    public Ability(Identifier id) {
        this(id, null);
    }

    public Ability(Identifier id, Ability base) {
        this.id = id;
        this.baseId = (base != null) ? base.id : id;
    }

    public int rollFor(LivingEntity ent, int diceMax, int diceCount) {
        return Dice.roll(diceMax, diceCount, this.modifierFor(ent));
    }

    public int modifierFor(LivingEntity ent) {
        if (ent == null) {
            return 0;
        }
        var maybeComp = ModComponents.ABILITIES.maybeGet(ent);
        if (maybeComp.isEmpty()) {
            return 0;
        }
        var comp = maybeComp.get();

        var value = comp.abilityValue(this.baseId, DEFAULT_ABILITY_VALUE);
        var modifier = (value - 10) / 2;
        if (!this.id.equals(this.baseId) && comp.isProficient(this.id)) {
            modifier += comp.getProficiencyModifier();
        }
        return modifier;
    }

    @Override
    public String toString() {
        return "Ability{"+this.id.toString()+"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ability ability = (Ability) o;
        return id.equals(ability.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
