package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Function;

public class SpellBuilder {
    private final @NotNull Entity caster;
    private final int maxElements;
    private final List<SpellElement> fullRecipe;
    private final List<SpellElement> pendingElements;
    private Spell spell;
    private final List<SpellReaction> reactions;
    private final Map<String, Float> spellAspectCosts;

    public SpellBuilder(@NotNull Entity caster, int maxElements) {
        this.caster = caster;
        this.maxElements = maxElements;
        this.fullRecipe = new ArrayList<>();
        this.pendingElements = new ArrayList<>();
        this.reactions = new ArrayList<>();
        this.spellAspectCosts = new HashMap<>();
    }

    public void addElement(@NotNull SpellElement element) {
        if (this.pendingElements.size() == this.maxElements) {
            return;
        }

        this.pendingElements.add(element);
        this.fullRecipe.add(element);

        RPGKitMod.LOGGER.debug("SpellBuilder.addElement: {}", element);

        if (this.pendingElements.size() >= 2) {
            var lastIndex = this.pendingElements.size() - 1;
            var res = ModRegistries.ASPECT_RECIPES.tryMatch(List.of(
                    this.pendingElements.get(lastIndex - 1),
                    this.pendingElements.get(lastIndex)
            ));
            if (res != null) {
                RPGKitMod.LOGGER.debug("SpellBuilder.addElement: Aspects {}, {} merged into {}",
                        this.pendingElements.get(lastIndex - 1), this.pendingElements.get(lastIndex), res);
                this.pendingElements.remove(this.pendingElements.size() - 1);
                this.pendingElements.remove(this.pendingElements.size() - 1);
                this.pendingElements.add(res.result());

                this.fullRecipe.remove(this.fullRecipe.size() - 1);
                this.fullRecipe.remove(this.fullRecipe.size() - 1);
                this.fullRecipe.add(res.result());
            }
        }
    }

    public @NotNull SpellForm determineUseForm() {
        return this.determinePendingSpell().determineUseForm();
    }

    public @NotNull Spell determinePendingSpell() {
        var spell = ModRegistries.SPELL_RECIPES.tryMatch(this.pendingElements);
        if (spell != null) {
            return spell.result();
        }
        return new GenericSpell(this, ImmutableList.copyOf(this.pendingElements));
    }

    public void finishSpell() {
        if (this.spell != null) {
            throw new IllegalStateException("Spell is already finished");
        }

        var spell = ModRegistries.SPELL_RECIPES.tryMatch(this.pendingElements);
        if (spell != null) {
            this.spell = spell.result();
            this.consumeElements(this.pendingElements, (i) -> spell.elements().get(i).consume());
        } else {
            var generic = new GenericSpell(this, ImmutableList.copyOf(this.pendingElements));
            this.reactions.addAll(generic.getForcedEffectReactions());
            this.spell = generic;
        }

        for (var element : this.pendingElements) {
            for (var key : Aspect.COST_ALL) {
                this.spellAspectCosts.merge(key, element.getBaseCost(key), Float::sum);
            }
        }

        this.pendingElements.clear();

        RPGKitMod.LOGGER.debug("SpellBuilder.finishSpell: {}", this.spell);
    }

    public void finishReaction() {
        var reactionFound = false;
        var shouldConsume = new boolean[this.pendingElements.size()];
        for (SpellEffect effect : this.spell.getEffects()) {
            var effectReaction = ModRegistries.REACTION_RECIPES.tryMatch(this.pendingElements, reaction -> reaction.appliesTo(effect));
            if (effectReaction != null) {
                this.reactions.add(effectReaction.result());
                for (var entry : this.spellAspectCosts.entrySet()) {
                    this.spellAspectCosts.compute(entry.getKey(),
                            (k, v) -> effectReaction.result().applyCost(k, v == null ? 0 : v));
                }

                // Consume effect reaction elements if any of matched recipes mark it as consumable.
                for (int i = 0; i < effectReaction.elements().size(); i++) {
                    shouldConsume[i] = shouldConsume[i] || effectReaction.elements().get(i).consume();
                }
                reactionFound = true;
            }
        }

        var formReactions = ModRegistries.REACTION_RECIPES.tryMatchMultiple(this.pendingElements);
        if (formReactions != null) {
            for (var reaction : formReactions) {
                this.reactions.add(reaction.result());

                for (int i = 0; i < reaction.elements().size(); i++) {
                    shouldConsume[i] = shouldConsume[i] || reaction.elements().get(i).consume();
                }
            }

            reactionFound = true;
        }

        if (reactionFound) {
            this.consumeElements(this.pendingElements, (i) -> shouldConsume[i]);
            this.pendingElements.clear();
        }
    }

    public ServerSpellCast toServerCast(@NotNull SpellForm form) {
        if (this.spell == null) {
            this.finishSpell();
        }

        var costs = this.calculateFormedCosts(form);

        RPGKitMod.LOGGER.info("{} casting spell {} with form {} and reactions {} (elements: {})",
                caster, this.spell, form, this.reactions, this.fullRecipe);
        RPGKitMod.LOGGER.debug("Cast costs: {}", costs);
        return new ServerSpellCast(form, this.spell, this.caster, this.reactions,
                costs, this.fullRecipe, caster.getPos(), caster.getHeadYaw(), caster.getPitch());
    }

    private void consumeElements(List<SpellElement> elements, Function<Integer, Boolean> shouldConsume) {
        for (int i = 0; i < elements.size(); i++) {
            if (shouldConsume.apply(i)) {
                // TODO: Check that element is still usable (item still present, etc)

                elements.get(i).consume();
            }
        }
    }

    private Map<String, Float> calculateFormedCosts(SpellForm form) {
        var formedCosts = new HashMap<String, Float>();

        for (var element : this.spellAspectCosts.entrySet()) {
            var cost = element.getValue();

            cost = form.applyCost(element.getKey(), cost);

            for (var reaction : this.reactions) {
                if (!reaction.appliesTo(form)) continue;
                cost = reaction.applyCost(element.getKey(), cost);
            }

            formedCosts.put(element.getKey(), cost);
        }

        return formedCosts;
    }

    public @Unmodifiable List<SpellElement> getPendingElements() {
        return Collections.unmodifiableList(this.pendingElements);
    }

    public @Unmodifiable List<SpellElement> getFullRecipe() {
        return Collections.unmodifiableList(fullRecipe);
    }

    public int getMaxElements() {
        return maxElements;
    }

    public @Nullable Entity getCaster() {
        return this.caster;
    }
}
