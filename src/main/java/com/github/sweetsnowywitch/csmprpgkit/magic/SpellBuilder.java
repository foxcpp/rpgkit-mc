package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.ItemEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public final class SpellBuilder {
    private final @NotNull Entity caster;
    private final int maxElements;
    private final List<SpellElement> fullRecipe;
    private final List<SpellElement> pendingElements;
    private @Nullable Spell spell;
    private final Map<String, Float> spellAspectCosts;

    public SpellBuilder(@NotNull Entity caster, int maxElements) {
        this.caster = caster;
        this.maxElements = maxElements;
        this.fullRecipe = new ArrayList<>();
        this.pendingElements = new ArrayList<>();
        this.spell = null;
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
            var res = MagicRegistries.ASPECT_RECIPES.tryMatch(List.of(
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
        return this.determinePendingSpell().getUseForm();
    }

    public @NotNull Spell determinePendingSpell() {
        var itemEffects = new ImmutableList.Builder<ItemEffect.Used>();
        var areaEffects = new ImmutableList.Builder<AreaEffect.Used>();
        var useEffects = new ImmutableList.Builder<UseEffect.Used>();
        var formReactions = new ImmutableList.Builder<SpellReaction>();

        SpellForm useForm = ModForms.RAY;
        int useFormWeight = -9999;

        var ctx = new SpellBuildCondition.Context();
        ctx.caster = null;
        ctx.elements = this.pendingElements;

        for (var el : this.pendingElements) {
            ctx.element = el;

            el.itemEffects().stream().filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).forEach(itemEffects::add);
            el.areaEffects().stream().filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).forEach(areaEffects::add);
            el.useEffects().stream().filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).forEach(useEffects::add);
            el.formReactions().stream().filter(eff -> eff.shouldAdd(ctx)).forEach(formReactions::add);

            if (el.getPreferredForm() != null && el.getPreferredFormWeight() > useFormWeight) {
                useForm = el.getPreferredForm();
                useFormWeight = el.getPreferredFormWeight();
            }
        }

        return new Spell(
                itemEffects.build(), areaEffects.build(), useEffects.build(),
                formReactions.build(),
                useForm);
    }

    public void finishSpell() {
        if (this.spell != null) {
            throw new IllegalStateException("Spell is already finished");
        }

        this.spell = this.determinePendingSpell();

        for (var element : this.pendingElements) {
            for (var key : Aspect.COST_ALL) {
                this.spellAspectCosts.merge(key, element.getBaseCost(key), Float::sum);
            }
        }

        this.pendingElements.clear();

        RPGKitMod.LOGGER.debug("SpellBuilder.finishSpell: {}", this.spell);
    }

    public ServerSpellCast toServerCast(@NotNull SpellForm form) {
        if (this.spell == null) {
            this.finishSpell();
        }

        var costs = this.calculateFormedCosts(form);

        RPGKitMod.LOGGER.info("{} casting spell {} with form {} (elements: {})",
                caster, this.spell, form, this.fullRecipe);
        RPGKitMod.LOGGER.debug("Cast costs: {}", costs);
        return new ServerSpellCast(form, this.spell, this.caster,
                costs, this.fullRecipe, caster.getPos(), caster.getHeadYaw(), caster.getPitch());
    }

    private Map<String, Float> calculateFormedCosts(SpellForm form) {
        var formedCosts = new HashMap<String, Float>();

        for (var element : this.spellAspectCosts.entrySet()) {
            var cost = element.getValue();

            cost = form.applyCost(element.getKey(), cost);

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

    public @NotNull Entity getCaster() {
        return this.caster;
    }
}
