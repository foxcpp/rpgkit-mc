package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;

public class GenericSpell extends Spell {
    public static final GenericSpell EMPTY = new GenericSpell(ImmutableList.of());
    private final ImmutableList<SpellElement> elements;
    private final ImmutableList<SpellReaction> forcedEffectReactions;

    public GenericSpell(ImmutableList<SpellElement> elements) {
        super(Identifier.of(RPGKitMod.MOD_ID, "generic"), computeEffects(elements));
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

    public ImmutableList<SpellReaction> getForcedEffectReactions() {
        return forcedEffectReactions;
    }
}
