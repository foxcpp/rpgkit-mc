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
    private final SpellForm form;
    private Spell spell;
    private final List<SpellReaction> formReactions = new ArrayList<>();
    private final List<SpellReaction> effectReactions = new ArrayList<>();

    private final Map<String, Float> spellAspectCosts = new HashMap<>();

    private final Map<String, Float> formedCosts = new HashMap<>();

    public SpellBuilder(SpellForm form) {
        this.form = form;
    }

    public void addElement(@NotNull SpellElement element) {
        this.pendingElements.add(element);
        this.fullRecipe.add(element);

        this.buildRecipe();
    }

    public void complete() {
        // TODO: add dummy effects if incomplete spell is cast (no spell itself, incomplete reaction, etc).
    }

    public SpellCast toCast(LivingEntity caster) {
        RPGKitMod.LOGGER.info("{} casting spell {} with form {} ({}) and effect reactions {} (elements: {})",
                caster, this.spell, this.form, this.formReactions, this.effectReactions, this.fullRecipe);
        RPGKitMod.LOGGER.debug("Cast costs: {}", this.formedCosts);
        return new SpellCast(this.form, this.spell, caster, this.formReactions, this.effectReactions);
    }

    public SpellForm getForm() {
        return this.form;
    }

    public @Nullable Spell getSpell() {
        return this.spell;
    }

    public List<SpellReaction> getFormReactions() {
        return this.formReactions;
    }

    public List<SpellReaction> getEffectReactions() {
        return this.effectReactions;
    }

    public List<SpellElement> fullRecipe() {
        return this.fullRecipe;
    }

    private void buildRecipe() {
        if (this.spell == null) {
            var spell = ModRegistries.SPELL_RECIPES.tryMatch(this.pendingElements);
            if (spell != null) {
                this.spell = spell;
                for (var element : this.pendingElements) {
                    for (var key : Aspect.COST_ALL) {
                        this.spellAspectCosts.merge(key, element.getBaseCost(key), Float::sum);
                    }
                }
                this.pendingElements.clear();
            }
            return;
        }

        var formReaction = ModRegistries.REACTION_RECIPES.tryMatch(this.pendingElements, reaction -> reaction.appliesTo(this.form));
        if (formReaction != null) {
            this.formReactions.add(formReaction);
            this.pendingElements.clear();
            this.updateFormedCosts();
            return;
        }

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
        if (reactionFound) {
            this.pendingElements.clear();
            this.updateFormedCosts();
        }
    }

    private void updateFormedCosts() {
        this.formedCosts.clear();
        for (var element : this.spellAspectCosts.entrySet()) {
            var cost = element.getValue();

            cost = this.form.applyCost(element.getKey(), cost);

            for (var reaction : this.formReactions) {
                cost = reaction.applyCost(element.getKey(), cost);
            }

            this.formedCosts.put(element.getKey(), cost);
        }
    }
}
