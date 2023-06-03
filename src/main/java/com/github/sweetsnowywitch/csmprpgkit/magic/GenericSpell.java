package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;

public class GenericSpell extends Spell {
    public static final GenericSpell EMPTY = new GenericSpell();
    private final ImmutableList<SpellElement> elements;
    private final ImmutableList<SpellReaction> forcedEffectReactions;

    private GenericSpell() {
        super(new Identifier(RPGKitMod.MOD_ID, "generic"), ImmutableList.of(), null);
        this.elements = ImmutableList.of();
        this.forcedEffectReactions = ImmutableList.of();
    }

    private GenericSpell(ImmutableList<SpellElement> elements, Set<Identifier> allowedEffects, Set<Identifier> allowedReactions) {
        super(new Identifier(RPGKitMod.MOD_ID, "generic"), computeEffects((effect, el) -> allowedEffects.contains(effect.id), elements), null);
        this.elements = elements;
        this.forcedEffectReactions = computeReactions((reaction, el) -> allowedReactions.contains(reaction.id), elements);
    }

    public GenericSpell(SpellBuilder builder, ImmutableList<SpellElement> elements) {
        super(new Identifier(RPGKitMod.MOD_ID, "generic"), computeEffects((effect, el) -> effect.shouldAdd(builder, el), elements), null);
        this.elements = elements;
        this.forcedEffectReactions = computeReactions((reaction, el) -> reaction.shouldAdd(builder, el), elements);
    }

    private static ImmutableList<SpellEffect> computeEffects(BiPredicate<SpellEffect, SpellElement> allowedEffects, List<SpellElement> elements) {
        HashSet<SpellElement> seen = new HashSet<>(elements.size());
        ImmutableList.Builder<SpellEffect> effects = ImmutableList.builder();
        for (var element : elements) {
            if (!seen.contains(element)) {
                effects.addAll(
                        element.getGenericEffects().stream().
                                filter(effect -> allowedEffects.test(effect, element)).
                                iterator());
                seen.add(element);
            }
        }
        return effects.build();
    }

    private static ImmutableList<SpellReaction> computeReactions(BiPredicate<SpellReaction, SpellElement> allowedReactions, List<SpellElement> elements) {
        HashSet<SpellElement> seen = new HashSet<>(elements.size());
        ImmutableList.Builder<SpellReaction> reactions = ImmutableList.builder();
        for (var element : elements) {
            if (seen.contains(element)) {
                reactions.addAll(element.getGenericReactions().stream().
                        filter(reaction -> allowedReactions.test(reaction, element)).
                        iterator());
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


        var allowedEffects = new HashSet<Identifier>();
        var allowedEffectsNbt = comp.getList("AllowedEffects", NbtElement.STRING_TYPE);
        if (allowedEffectsNbt != null) {
            for (var el : allowedEffectsNbt) {
                try {
                    allowedEffects.add(new Identifier(el.asString()));
                } catch (InvalidIdentifierException ex) {
                    RPGKitMod.LOGGER.warn("Missing or malformed AllowedEffects field in NBT, some spell effects will not be applied");
                }
            }
        } else {
            RPGKitMod.LOGGER.warn("Missing or malformed AllowedEffects field in NBT, spell effects will not be applied");
        }

        var allowedReactions = new HashSet<Identifier>();
        var allowedReactionsNbt = comp.getList("AllowedReactions", NbtElement.STRING_TYPE);
        if (allowedReactionsNbt != null) {
            for (var el : allowedReactionsNbt) {
                try {
                    allowedReactions.add(new Identifier(el.asString()));
                } catch (InvalidIdentifierException ex) {
                    RPGKitMod.LOGGER.warn("Missing or malformed AllowedReactions field in NBT, some spell effects will not be applied");
                }
            }
        } else {
            RPGKitMod.LOGGER.warn("Missing or malformed AllowedReactions field in NBT, spell effects will not be applied");
        }

        return new GenericSpell(elements.build(), allowedEffects, allowedReactions);
    }

    @Override
    public void writeToNbt(NbtCompound comp) {
        super.writeToNbt(comp);

        // HACK: We save results of evaluated SpellBuildCondition as a set of
        // allowed effects/reactions instead of serializing the whole effect/reaction config.

        var allowedEffects = new NbtList();
        for (var effect : this.getEffects()) {
            allowedEffects.add(NbtString.of(effect.id.toString()));
        }
        comp.put("AllowedEffects", allowedEffects);

        var allowedReactions = new NbtList();
        for (var reaction : this.getForcedEffectReactions()) {
            allowedReactions.add(NbtString.of(reaction.id.toString()));
        }
        comp.put("AllowedReactions", allowedReactions);

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
