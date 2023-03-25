package com.github.sweetsnowywitch.csmprpgkit.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AbilitiesComponent implements ComponentV3 {
    private final LivingEntity provider;
    private Map<String, Integer> abilityValues;
    private Set<String> proficiency;
    private int proficiencyModifier;

    public AbilitiesComponent(LivingEntity provider) {
        this.provider = provider;
        this.abilityValues = new HashMap<>();
        this.proficiency = new HashSet<>();
        this.proficiencyModifier = 0;
    }

    public int abilityValue(String id, int defaultValue) {
        return this.abilityValues.getOrDefault(id, defaultValue);
    }

    public boolean isProficient(String abilityId) {
        return this.proficiency.contains(abilityId);
    }

    public int getProficiencyModifier() {
        return this.proficiencyModifier;
    }

    public void setAbilityValue(String id, int value) {
        this.abilityValues.put(id, value);
        ModComponents.ABILITIES.sync(this.provider);
    }

    public void setProficiency(String abilityId, boolean proficient) {
        if (proficient) {
            this.proficiency.add(abilityId);
        } else {
            this.proficiency.remove(abilityId);
        }
        ModComponents.ABILITIES.sync(this.provider);
    }

    public void setProficiencyModifier(int newValue) {
        this.proficiencyModifier = newValue;
        ModComponents.ABILITIES.sync(this.provider);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        var abilitiesNBT = tag.getCompound("abilities");
        for (var ability : abilitiesNBT.getKeys()) {
            this.abilityValues.put(ability, abilitiesNBT.getInt(ability));
        }

        for (var prof : tag.getList("proficiency", NbtElement.STRING_TYPE)) {
            this.proficiency.add(prof.asString());
        }

        this.proficiencyModifier = tag.getInt("proificiencyModifier");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        var abilities = new NbtCompound();
        for (Map.Entry<String, Integer> ent : this.abilityValues.entrySet()) {
            abilities.putInt(ent.getKey(), ent.getValue());
        }

        var proficiency = new NbtList();
        for (String value : this.proficiency) {
            proficiency.add(NbtString.of(value));
        }

        tag.put("abilities", abilities);
        tag.put("proficiency", proficiency);
        tag.putInt("proficiencyModifier", this.proficiencyModifier);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbilitiesComponent other)) {
            return false;
        }
        return other.proficiency.equals(this.proficiency) &&
                other.proficiencyModifier == this.proficiencyModifier &&
                other.abilityValues.equals(this.abilityValues);
    }
}
