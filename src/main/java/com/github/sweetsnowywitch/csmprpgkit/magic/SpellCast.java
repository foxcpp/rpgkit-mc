package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SpellCast {
    private final SpellForm form;
    private final Spell spell;
    private final LivingEntity caster;
    private final UUID casterUuid;
    private final World world;
    private final ImmutableList<SpellReaction> formReactions;
    private final ImmutableList<SpellReaction> effectReactions;
    private final ImmutableMap<String, Float> costs;

    public SpellCast(SpellForm form, Spell spell, World world, UUID casterID, List<SpellReaction> formReactions,
                     List<SpellReaction> effectReactions, Map<String, Float> costs) {
        this.form = form;
        this.spell = spell;
        this.caster = null;
        this.casterUuid = casterID;
        this.world = world;
        this.formReactions = ImmutableList.copyOf(formReactions);
        this.effectReactions = ImmutableList.copyOf(effectReactions);
        this.costs = ImmutableMap.copyOf(costs);
    }

    public SpellCast(SpellForm form, Spell spell, @NotNull LivingEntity caster, List<SpellReaction> formReactions,
                     List<SpellReaction> effectReactions, Map<String, Float> costs) {
        this.form = form;
        this.spell = spell;
        this.caster = caster;
        this.casterUuid = caster.getUuid();
        this.world = caster.getWorld();
        this.formReactions = ImmutableList.copyOf(formReactions);
        this.effectReactions = ImmutableList.copyOf(effectReactions);
        this.costs = ImmutableMap.copyOf(costs);
    }

    public void perform() {
        if (this.world.isClient) {
            throw new IllegalStateException("cannot perform SpellCast on the client");
        }


        this.form.startCast(this);

        if (this.caster != null && this.caster instanceof PlayerEntity player) {
            var cost = this.costs.getOrDefault(SpellElement.COST_MAGICAE, (float)0);
            if (cost != null) {
                RPGKitMod.LOGGER.debug("Consuming {} mana points of {}", cost, player);
                player.getComponent(ModComponents.MANA).spendMana((int)((float)cost));
            }
        }
    }

    public SpellForm getForm() {
        return form;
    }

    public Spell getSpell() {
        return spell;
    }

    public World getWorld() { return world; }

    public LivingEntity getCaster() {
        return this.caster;
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

    public void writeToNbt(NbtCompound nbt) {
        var formID = ModRegistries.SPELL_FORMS.getId(this.form);
        if (formID == null) throw new IllegalStateException("writeToNbt called with unregistered spell form: %s".formatted(this.form));
        nbt.putString("form", formID.toString());

        var spellID = ModRegistries.SPELLS.inverse().get(this.spell);
        if (spellID == null) throw new IllegalStateException("writeToNbt called with unregistered spell: %s".formatted(this.spell));
        nbt.putString("spell", spellID.toString());

        nbt.putUuid("caster", this.casterUuid);

        var formReactions = new NbtList();
        for (var reaction : this.formReactions) {
            var reactionID = ModRegistries.REACTIONS.inverse().get(reaction);
            if (reactionID == null) throw new IllegalStateException("writeToNbt called with unregistered reaction: %s".formatted(reaction));
            formReactions.add(NbtString.of(reactionID.toString()));
        }
        nbt.put("formReactions", formReactions);

        var effectReactions = new NbtList();
        for (var reaction : this.effectReactions) {
            var reactionID = ModRegistries.REACTIONS.inverse().get(reaction);
            if (reactionID == null) throw new IllegalStateException("writeToNbt called with unregistered reaction: %s".formatted(reaction));
            effectReactions.add(NbtString.of(reactionID.toString()));
        }
        nbt.put("effectReactions", effectReactions);

        var costs = new NbtCompound();
        for (var ent : this.costs.entrySet()) {
            costs.putFloat(ent.getKey(), ent.getValue());
        }
        nbt.put("costs", costs);
    }

    public static SpellCast readFromNbt(NbtCompound nbt, World world) {
        var formID = Identifier.tryParse(nbt.getString("form"));
        if (formID == null) throw new IllegalArgumentException("malformed form identifier in NBT: %s".formatted(nbt.getString("form")));
        var form = ModRegistries.SPELL_FORMS.get(formID);
        if (form == null) throw new IllegalArgumentException("unknown form in NBT: %s".formatted(formID));

        var spellID = Identifier.tryParse(nbt.getString("spell"));
        if (spellID == null) throw new IllegalArgumentException("malformed spell identifier in NBT: %s".formatted(nbt.getString("spell")));
        var spell = ModRegistries.SPELLS.get(formID);
        if (spell == null) throw new IllegalArgumentException("unknown spell in NBT: %s".formatted(spellID));

        var casterID = nbt.getUuid("caster");
        if (casterID == null) throw new IllegalArgumentException("missing caster UUID in NBT");

        ImmutableList.Builder<SpellReaction> formReactions = ImmutableList.builder();
        var formReactionsNbt = nbt.getList("formReactions", NbtElement.STRING_TYPE);
        for (var element : formReactionsNbt) {
            var reactionID = Identifier.tryParse(element.asString());
            if (reactionID == null) throw new IllegalArgumentException("malformed reaction identifier in NBT: %s".formatted(element.asString()));
            var reaction = ModRegistries.REACTIONS.get(formID);
            if (reaction == null) throw new IllegalArgumentException("unknown reaction in NBT: %s".formatted(formID));
            formReactions.add(reaction);
        }

        ImmutableList.Builder<SpellReaction> effectReactions = ImmutableList.builder();
        var effectReactionsNbt = nbt.getList("effectReactions", NbtElement.STRING_TYPE);
        for (var element : effectReactionsNbt) {
            var reactionID = Identifier.tryParse(element.asString());
            if (reactionID == null) throw new IllegalArgumentException("malformed reaction identifier in NBT: %s".formatted(element.asString()));
            var reaction = ModRegistries.REACTIONS.get(formID);
            if (reaction == null) throw new IllegalArgumentException("unknown reaction in NBT: %s".formatted(formID));
            effectReactions.add(reaction);
        }

        ImmutableMap.Builder<String, Float> costs = ImmutableMap.builder();
        var costsNbt = nbt.getCompound("costs");
        for (var key : costsNbt.getKeys()) {
            costs.put(key, costsNbt.getFloat(key));
        }

        return new SpellCast(form, spell, world, casterID, formReactions.build(), effectReactions.build(), costs.build());
    }

    @Override
    public String toString() {
        return "SpellSpell[form=%s,spell=%s,caster=%s]".formatted(this.form.toString(), this.spell.toString(), this.caster.toString());
    }
}
