package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Array;
import java.util.*;

/**
 * ClientSpellCast is a limited data object representing what both client
 * and server should know about spell cast.
 *
 * @see ServerSpellCast
 */
public class SpellCast {
    public static final TrackedDataHandler<SpellCast> TRACKED_HANDLER = new TrackedDataHandler.ImmutableHandler<SpellCast>() {
        @Override
        public void write(PacketByteBuf buf, SpellCast value) {
            var nbt = new NbtCompound();
            value.writeToNbt(nbt);
            buf.writeNbt(nbt);
        }

        @Override
        public SpellCast read(PacketByteBuf buf) {
            var nbt = buf.readNbt();
            return SpellCast.readFromNbt(nbt);
        }
    };

    public static final SpellCast EMPTY = new SpellCast(ModForms.SELF, Spell.EMPTY,
                                              List.of(), List.of(), Map.of(), List.of());

    protected final SpellForm form;
    protected final Spell spell;
    protected final ImmutableList<SpellReaction> formReactions;
    protected final ImmutableList<SpellReaction> effectReactions;
    protected final ImmutableMap<String, Float> costs;
    protected final ImmutableList<SpellElement> fullRecipe;

    public SpellCast(SpellForm form, Spell spell, List<SpellReaction> formReactions,
                           List<SpellReaction> effectReactions, Map<String, Float> costs, List<SpellElement> fullRecipe) {
        this.form = form;
        this.spell = spell;
        this.formReactions = ImmutableList.copyOf(formReactions);
        this.effectReactions = ImmutableList.copyOf(effectReactions);
        this.costs = ImmutableMap.copyOf(costs);
        this.fullRecipe = ImmutableList.copyOf(fullRecipe);
    }

    protected SpellCast(NbtCompound nbt) {
        var formID = Identifier.tryParse(nbt.getString("Form"));
        if (formID == null) throw new IllegalArgumentException("malformed form identifier in NBT: %s".formatted(nbt.getString("form")));
        var form = ModRegistries.SPELL_FORMS.get(formID);
        if (form == null) {
            RPGKitMod.LOGGER.warn("Unknown form ID in NBT, replacing with SELF: {}", formID);
            form = ModForms.SELF;
        }

        var spellID = Identifier.tryParse(nbt.getString("Spell"));
        if (spellID == null) throw new IllegalArgumentException("malformed spell identifier in NBT: %s".formatted(nbt.getString("spell")));
        var spell = ModRegistries.SPELLS.get(spellID);
        if (spell == null) {
            RPGKitMod.LOGGER.warn("Unknown spell ID in NBT, replacing with EMPTY: {}", spellID);
            spell = Spell.EMPTY;
        }

        var casterID = nbt.getUuid("Caster");
        if (casterID == null) throw new IllegalArgumentException("missing caster UUID in NBT");

        ImmutableList.Builder<SpellReaction> formReactions = ImmutableList.builder();
        var formReactionsNbt = nbt.getList("FormReactions", NbtElement.STRING_TYPE);
        for (var element : formReactionsNbt) {
            var reactionID = Identifier.tryParse(element.asString());
            if (reactionID == null) throw new IllegalArgumentException("malformed reaction identifier in NBT: %s".formatted(element.asString()));
            var reaction = ModRegistries.REACTIONS.get(reactionID);
            if (reaction == null) {
                RPGKitMod.LOGGER.warn("Unknown form reaction ID in NBT, discarding: {}", reactionID);
                continue;
            }
            formReactions.add(reaction);
        }

        ImmutableList.Builder<SpellReaction> effectReactions = ImmutableList.builder();
        var effectReactionsNbt = nbt.getList("EffectReactions", NbtElement.STRING_TYPE);
        for (var element : effectReactionsNbt) {
            var reactionID = Identifier.tryParse(element.asString());
            if (reactionID == null) throw new IllegalArgumentException("malformed reaction identifier in NBT: %s".formatted(element.asString()));
            var reaction = ModRegistries.REACTIONS.get(reactionID);
            if (reaction == null) {
                RPGKitMod.LOGGER.warn("Unknown effect reaction ID in NBT, discarding: {}", reactionID);
                continue;
            }
            effectReactions.add(reaction);
        }

        ImmutableMap.Builder<String, Float> costs = ImmutableMap.builder();
        var costsNbt = nbt.getCompound("Costs");
        for (var key : costsNbt.getKeys()) {
            costs.put(key, costsNbt.getFloat(key));
        }

        ImmutableList.Builder<SpellElement> fullRecipe = ImmutableList.builder();
        var fullRecipeNbt = nbt.getList("Recipe", NbtElement.COMPOUND_TYPE);
        for (var element : fullRecipeNbt) {
            var elementNBT = (NbtCompound)element;
            if (elementNBT.contains("Aspect")) {
                var aspectID = Identifier.tryParse(elementNBT.getString("Aspect"));
                if (aspectID == null) throw new IllegalArgumentException("malformed aspect identifier in NBT: %s".formatted(elementNBT.asString()));
                var aspect = ModRegistries.ASPECTS.get(aspectID);
                if (aspect == null) {
                    RPGKitMod.LOGGER.warn("Unknown aspect in spell cast recipe, ignoring: {}", aspectID);
                    continue;
                }
                fullRecipe.add(aspect);
            } else if (elementNBT.contains("Item")) {
                var itemID = Identifier.tryParse(elementNBT.getString("Item"));
                if (itemID == null) throw new IllegalArgumentException("malformed item identifier in NBT: %s".formatted(elementNBT.asString()));
                var item = Registry.ITEM.get(itemID);
                if (item.equals(Items.AIR)) {
                    RPGKitMod.LOGGER.warn("Unknown item in spell cast recipe, ignoring: {}", itemID);
                    continue;
                }
                fullRecipe.add(SpellElement.of(item));
            }
        }

        this.form = form;
        this.spell = spell;
        this.formReactions = formReactions.build();
        this.effectReactions = effectReactions.build();
        this.costs = costs.build();
        this.fullRecipe = fullRecipe.build();
    }

    public static SpellCast readFromNbt(NbtCompound nbt) {
        return new SpellCast(nbt);
    }

    public void writeToNbt(NbtCompound nbt) {
        var formID = ModRegistries.SPELL_FORMS.getId(this.form);
        if (formID == null) throw new IllegalStateException("writeToNbt called with unregistered spell form: %s".formatted(this.form));
        nbt.putString("Form", formID.toString());

        nbt.putString("Spell", this.spell.id.toString());

        var formReactions = new NbtList();
        for (var reaction : this.formReactions) {
            formReactions.add(NbtString.of(reaction.id.toString()));
        }
        nbt.put("FormReactions", formReactions);

        var effectReactions = new NbtList();
        for (var reaction : this.effectReactions) {
            effectReactions.add(NbtString.of(reaction.id.toString()));
        }
        nbt.put("EffectReactions", effectReactions);

        var costs = new NbtCompound();
        for (var ent : this.costs.entrySet()) {
            costs.putFloat(ent.getKey(), ent.getValue());
        }
        nbt.put("Costs", costs);

        var fullRecipe = new NbtList();
        for (var element : this.fullRecipe) {
            var elementNBT = new NbtCompound();
            if (element instanceof Aspect asp) {
                elementNBT.putString("Aspect", asp.id.toString());
            } else if (element instanceof ItemElement ie) {
                elementNBT.putString("Item", Registry.ITEM.getId(ie.getItem()).toString());
            }
            fullRecipe.add(elementNBT);
        }
        nbt.put("Recipe", fullRecipe);
    }

    public SpellForm getForm() {
        return form;
    }

    public Spell getSpell() {
        return spell;
    }
    public ImmutableList<SpellReaction> getFormReactions() {
        return formReactions;
    }

    public float getCost(String key) {
        return Objects.requireNonNull(this.costs.getOrDefault(key, 0f));
    }

    public ImmutableList<SpellReaction> getEffectReactions() {
        return effectReactions;
    }

    public ImmutableList<SpellElement> getFullRecipe() {
        return this.fullRecipe;
    }

    public int calculateBaseColor() {
        int[] elementColors = new int[3];
        int i = 0;

        for (var element : this.getFullRecipe()) {
            if (element instanceof Aspect) {
                elementColors[i] = element.getColor() + 0x10000000;
                i++;
                while (i >= 2) {
                    var calculatedColor = ColorHelper.Argb.mixColor(elementColors[0], elementColors[1]);
                    elementColors[1] = 0;
                    i = 1;
                    elementColors[0] = calculatedColor;
                }
            }
        }
        return elementColors[0];
    }
}
