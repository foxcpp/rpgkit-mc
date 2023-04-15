package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellBuilder {
    private final List<SpellElement> fullRecipe = new ArrayList<>();
    private final List<SpellElement> pendingElements = new ArrayList<>();
    private Spell spell;
    private final List<SpellReaction> formReactions = new ArrayList<>();
    private final List<SpellReaction> effectReactions = new ArrayList<>();
    private final Map<String, Float> spellAspectCosts = new HashMap<>();

    public SpellBuilder() {}

    public void addElement(@NotNull SpellElement element) {
        this.pendingElements.add(element);
        this.fullRecipe.add(element);

        RPGKitMod.LOGGER.debug("SpellBuilder.addElement: {}", element);

        // TODO: Check elements merging.
    }

    public void finishSpell() {
        if (this.spell != null) {
            throw new IllegalStateException("Spell is already finished");
        }

        var spell = ModRegistries.SPELL_RECIPES.tryMatch(this.pendingElements);
        if (spell != null) {
            this.spell = spell;
            for (var element : this.pendingElements) {
                for (var key : Aspect.COST_ALL) {
                    this.spellAspectCosts.merge(key, element.getBaseCost(key), Float::sum);
                }
            }
            this.pendingElements.clear();
        } else {
            // TODO: Construct generic spell.
            this.spell = Spell.EMPTY;
        }

        RPGKitMod.LOGGER.debug("SpellBuilder.finishSpell: {}", this.spell);
    }

    public void finishReaction() {
        var reactionFound = false;
        for (SpellEffect effect : this.spell.getEffects()) {
            var effectReaction = ModRegistries.REACTION_RECIPES.tryMatch(this.pendingElements, reaction -> reaction.appliesTo(effect));
            if (effectReaction != null) {
                this.effectReactions.add(effectReaction);
                for (var entry : this.spellAspectCosts.entrySet()) {
                    this.spellAspectCosts.compute(entry.getKey(),
                            (k, v) -> effectReaction.applyCost(k, v == null ? 0 : v));
                }
                reactionFound = true;
            }
        }

        var formReaction = ModRegistries.REACTION_RECIPES.tryMatchMultiple(this.pendingElements);
        if (formReaction != null) {
            this.formReactions.addAll(formReaction);
            reactionFound = true;
        }

        if (reactionFound) {
            this.pendingElements.clear();
        }
    }

    public ServerSpellCast toCast(LivingEntity caster, SpellForm form) {
        var formReactions = this.formReactions.stream().filter((r) -> r.appliesTo(form)).toList();
        var costs = this.calculateFormedCosts(form, formReactions);

        RPGKitMod.LOGGER.info("{} casting spell {} with form {} ({}) and effect reactions {} (elements: {})",
                caster, this.spell, form, this.formReactions, this.effectReactions, this.fullRecipe);
        RPGKitMod.LOGGER.debug("Cast costs: {}", costs);
        return new ServerSpellCast(form, this.spell, caster, formReactions, this.effectReactions,
                costs, this.fullRecipe);
    }

    private Map<String, Float> calculateFormedCosts(SpellForm form, List<SpellReaction> formReactions) {
        var formedCosts = new HashMap<String, Float>();

        for (var element : this.spellAspectCosts.entrySet()) {
            var cost = element.getValue();

            cost = form.applyCost(element.getKey(), cost);

            for (var reaction : formReactions) {
                cost = reaction.applyCost(element.getKey(), cost);
            }

            formedCosts.put(element.getKey(), cost);
        }

        return formedCosts;
    }
}
