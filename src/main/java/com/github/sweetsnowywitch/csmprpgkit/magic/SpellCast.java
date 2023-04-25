package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;

/**
 * ClientSpellCast is a limited data object representing what both client
 * and server should know about spell cast.
 *
 * @see ServerSpellCast
 */
public class SpellCast {
    public static final TrackedDataHandler<SpellCast> TRACKED_HANDLER = new TrackedDataHandler.ImmutableHandler<>() {
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
                                              List.of(), Map.of(), List.of(), Vec3d.ZERO);

    protected final SpellForm form;
    protected final Spell spell;
    protected final ImmutableList<SpellReaction> reactions;
    protected final ImmutableMap<String, Float> costs;
    protected final ImmutableList<SpellElement> fullRecipe;
    protected final Vec3d startPos;

    public SpellCast(SpellForm form, Spell spell, List<SpellReaction> reactions,
                     Map<String, Float> costs, List<SpellElement> fullRecipe,
                            Vec3d startPos) {
        this.form = form;
        this.spell = spell;
        this.reactions = ImmutableList.copyOf(reactions);
        this.costs = ImmutableMap.copyOf(costs);
        this.fullRecipe = ImmutableList.copyOf(fullRecipe);
        this.startPos = startPos;
    }

    protected SpellCast(NbtCompound nbt) {
        var formID = Identifier.tryParse(nbt.getString("Form"));
        if (formID == null) throw new IllegalArgumentException("malformed form identifier in NBT: %s".formatted(nbt.getString("form")));
        var form = ModRegistries.SPELL_FORMS.get(formID);
        if (form == null) {
            RPGKitMod.LOGGER.warn("Unknown form ID in NBT, replacing with SELF: {}", formID);
            form = ModForms.SELF;
        }

        Spell spell;
        try {
            spell = Spell.readFromNbt(nbt.getCompound("Spell"));
        } catch (IllegalArgumentException e) {
            RPGKitMod.LOGGER.warn("Unknown spell ID in NBT, replacing with EMPTY: {}", nbt.get("Spell"));
            spell = Spell.EMPTY;
        }

        var casterID = nbt.getUuid("Caster");
        if (casterID == null) throw new IllegalArgumentException("missing caster UUID in NBT");

        ImmutableList.Builder<SpellReaction> reactions = ImmutableList.builder();
        var formReactionsNbt = nbt.getList("Reactions", NbtElement.STRING_TYPE);
        for (var element : formReactionsNbt) {
            var reactionID = Identifier.tryParse(element.asString());
            if (reactionID == null) throw new IllegalArgumentException("malformed reaction identifier in NBT: %s".formatted(element.asString()));
            var reaction = ModRegistries.REACTIONS.get(reactionID);
            if (reaction == null) {
                RPGKitMod.LOGGER.warn("Unknown reaction ID in NBT, discarding: {}", reactionID);
                continue;
            }
            reactions.add(reaction);
        }

        ImmutableMap.Builder<String, Float> costs = ImmutableMap.builder();
        var costsNbt = nbt.getCompound("Costs");
        for (var key : costsNbt.getKeys()) {
            costs.put(key, costsNbt.getFloat(key));
        }

        ImmutableList.Builder<SpellElement> fullRecipe = ImmutableList.builder();
        var fullRecipeNbt = nbt.getList("Recipe", NbtElement.COMPOUND_TYPE);
        for (var element : fullRecipeNbt) {
            try {
                fullRecipe.add(SpellElement.readFromNbt((NbtCompound) element));
            } catch (IllegalArgumentException e) {
                RPGKitMod.LOGGER.warn("Failed to load full recipe item, ignoring", e);
            }
        }

        this.form = form;
        this.spell = spell;
        this.reactions = reactions.build();
        this.costs = costs.build();
        this.fullRecipe = fullRecipe.build();
        this.startPos = new Vec3d(nbt.getDouble("StartX"), nbt.getDouble("StartY"), nbt.getDouble("StartZ"));
    }

    public static SpellCast readFromNbt(NbtCompound nbt) {
        return new SpellCast(nbt);
    }

    public void writeToNbt(NbtCompound nbt) {
        var formID = ModRegistries.SPELL_FORMS.getId(this.form);
        if (formID == null) throw new IllegalStateException("writeToNbt called with unregistered spell form: %s".formatted(this.form));
        nbt.putString("Form", formID.toString());

        var spellNBT = new NbtCompound();
        this.spell.writeToNbt(spellNBT);
        nbt.put("Spell", spellNBT);

        var formReactions = new NbtList();
        for (var reaction : this.reactions) {
            formReactions.add(NbtString.of(reaction.id.toString()));
        }
        nbt.put("Reactions", formReactions);

        var costs = new NbtCompound();
        for (var ent : this.costs.entrySet()) {
            costs.putFloat(ent.getKey(), ent.getValue());
        }
        nbt.put("Costs", costs);

        var fullRecipe = new NbtList();
        for (var element : this.fullRecipe) {
            var elementNBT = new NbtCompound();
            element.writeToNbt(elementNBT);
            fullRecipe.add(elementNBT);
        }
        nbt.put("Recipe", fullRecipe);

        nbt.putDouble("StartX", this.startPos.x);
        nbt.putDouble("StartY", this.startPos.y);
        nbt.putDouble("StartZ", this.startPos.z);
    }

    public SpellForm getForm() {
        return form;
    }

    public Spell getSpell() {
        return spell;
    }
    public ImmutableList<SpellReaction> getReactions() {
        return this.reactions;
    }

    public float getCost(String key) {
        return Objects.requireNonNull(this.costs.getOrDefault(key, 0f));
    }

    public ImmutableList<SpellElement> getFullRecipe() {
        return this.fullRecipe;
    }

    public Vec3d getStartPos() {
        return this.startPos;
    }
}
