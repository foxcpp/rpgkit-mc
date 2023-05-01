package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class GenericSpell extends Spell {
    public static final GenericSpell EMPTY = new GenericSpell(ImmutableList.of());
    private final ImmutableList<SpellElement> elements;
    private final ImmutableList<SpellReaction> forcedEffectReactions;

    public GenericSpell(ImmutableList<SpellElement> elements) {
        super(new Identifier(RPGKitMod.MOD_ID, "generic"), computeEffects(elements), null);
        this.elements = elements;
        this.forcedEffectReactions = computeReactions(elements);
    }

    private static ImmutableList<SpellEffect> computeEffects(List<SpellElement> elements) {
        HashSet<SpellElement> seen = new HashSet<>(elements.size());
        ImmutableList.Builder<SpellEffect> effects = ImmutableList.builder();
        for (var element : elements) {
            if (!seen.contains(element)) {
                effects.addAll(element.getGenericEffects());
                seen.add(element);
            }
        }
        return effects.build();
    }

    private static ImmutableList<SpellReaction> computeReactions(List<SpellElement> elements) {
        HashSet<SpellElement> seen = new HashSet<>(elements.size());
        ImmutableList.Builder<SpellReaction> reactions = ImmutableList.builder();
        for (var element : elements) {
            if (seen.contains(element)) {
                reactions.addAll(element.getGenericReactions());
            } else {
                seen.add(element);
            }
        }
        return reactions.build();
    }

    @Override
    protected Spell withNbt(NbtCompound comp) {
        ImmutableList.Builder<SpellElement> elements = ImmutableList.builder();
        var elementsNBT = comp.getList("Elements", NbtElement.COMPOUND_TYPE);
        for (var element : elementsNBT) {
            try {
                elements.add(SpellElement.readFromNbt((NbtCompound) element));
            } catch (IllegalArgumentException e) {
                RPGKitMod.LOGGER.warn("Failed to load full recipe item, ignoring", e);
            }
        }
        return new GenericSpell(elements.build());
    }

    @Override
    public void writeToNbt(NbtCompound comp) {
        super.writeToNbt(comp);

        var elementsNBT = new NbtList();
        for (var element : this.elements) {
            var elementNBT = new NbtCompound();
            element.writeToNbt(elementNBT);
            elementsNBT.add(elementNBT);
        }
        comp.put("Elements", elementsNBT);
    }

    @Override
    public @NotNull SpellForm determineUseForm() {
        var max = this.elements.stream().max(Comparator.comparingInt(SpellElement::getPreferredFormWeight));
        return max.map(spellElement -> Objects.requireNonNullElse(spellElement.getPreferredForm(), ModForms.RAY)).
                orElseGet(super::determineUseForm);
    }

    public ImmutableList<SpellReaction> getForcedEffectReactions() {
        return forcedEffectReactions;
    }
}
