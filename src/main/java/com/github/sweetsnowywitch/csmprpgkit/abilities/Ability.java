package com.github.sweetsnowywitch.csmprpgkit.abilities;

import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import net.minecraft.entity.LivingEntity;

public class Ability {
    public final String id;
    public final String baseId;

    public static int DEFAULT_ABILITY_VALUE = 10;

    public Ability(String id) {
        this(id, null);
    }

    public Ability(String id, Ability base) {
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
}
