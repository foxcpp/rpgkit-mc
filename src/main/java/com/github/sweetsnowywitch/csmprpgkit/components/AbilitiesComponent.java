package com.github.sweetsnowywitch.csmprpgkit.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AbilitiesComponent implements AutoSyncedComponent, ComponentV3 {
    private final LivingEntity provider;
    private final Map<Identifier, Integer> abilityValues;
    private final Set<Identifier> proficiency;
    private int proficiencyModifier;

    public AbilitiesComponent(LivingEntity provider) {
        this.provider = provider;
        this.abilityValues = new HashMap<>();
        this.proficiency = new HashSet<>();
        this.proficiencyModifier = 0;
    }

    public int abilityValue(Identifier id, int defaultValue) {
        return this.abilityValues.getOrDefault(id, defaultValue);
    }

    public boolean isProficient(Identifier abilityId) {
        return this.proficiency.contains(abilityId);
    }

    public int getProficiencyModifier() {
        return this.proficiencyModifier;
    }

    public void setAbilityValue(Identifier id, int value) {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot modify abilities client-side");
        }

        this.abilityValues.put(id, value);
        ModComponents.ABILITIES.sync(this.provider);
    }

    public void setProficiency(Identifier abilityId, boolean proficient) {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot modify abilities client-side");
        }

        if (proficient) {
            this.proficiency.add(abilityId);
        } else {
            this.proficiency.remove(abilityId);
        }
        ModComponents.ABILITIES.sync(this.provider);
    }

    public void setProficiencyModifier(int newValue) {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot modify abilities client-side");
        }

        this.proficiencyModifier = newValue;
        ModComponents.ABILITIES.sync(this.provider);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        var abilitiesNBT = tag.getCompound("Abilities");
        for (var ability : abilitiesNBT.getKeys()) {
            var abilityID = Identifier.tryParse(ability);
            if (abilityID == null) throw new IllegalArgumentException("malformed identifier in nbt: %s".formatted(ability));
            this.abilityValues.put(abilityID, abilitiesNBT.getInt(ability));
        }

        for (var prof : tag.getList("Proficiency", NbtElement.STRING_TYPE)) {
            var abilityID = Identifier.tryParse(prof.asString());
            if (abilityID == null) throw new IllegalArgumentException("malformed identifier in nbt: %s".formatted(prof));
            this.proficiency.add(abilityID);
        }

        this.proficiencyModifier = tag.getInt("ProificiencyModifier");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        var abilities = new NbtCompound();
        for (Map.Entry<Identifier, Integer> ent : this.abilityValues.entrySet()) {
            abilities.putInt(ent.getKey().toString(), ent.getValue());
        }

        var proficiency = new NbtList();
        for (Identifier value : this.proficiency) {
            proficiency.add(NbtString.of(value.toString()));
        }

        tag.put("Abilities", abilities);
        tag.put("Proficiency", proficiency);
        tag.putInt("ProficiencyModifier", this.proficiencyModifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbilitiesComponent other = (AbilitiesComponent) o;
        return other.proficiency.equals(this.proficiency) &&
                other.proficiencyModifier == this.proficiencyModifier &&
                other.abilityValues.equals(this.abilityValues);
    }
}
