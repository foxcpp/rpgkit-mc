package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class Spell {
    public final Identifier id;
    private final String translationKey;
    public static final Spell EMPTY = new Spell(new Identifier(RPGKitMod.MOD_ID, "empty"), ImmutableList.of(), null);
    private final ImmutableList<SpellEffect> effects;
    private final @Nullable SpellForm preferredUseForm;
    public Spell(@NotNull Identifier id, ImmutableList<SpellEffect> effects, @Nullable SpellForm preferredUseForm) {
        this.id = id;
        this.translationKey = id.toTranslationKey("csmprpgkit.magic.spell");
        this.effects = effects;
        this.preferredUseForm = preferredUseForm;
    }

    public ImmutableList<SpellEffect> getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spell spell = (Spell) o;
        return Objects.equals(id, spell.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Spell readFromNbt(NbtCompound comp) {
        var id = Identifier.tryParse(comp.getString("Id"));
        if (id == null) {
            throw new IllegalArgumentException("Malformed spell identifier in NBT: %s".formatted(comp.getString("Id")));
        }
        var spell = ModRegistries.SPELLS.get(id);
        if (spell == null) {
            throw new IllegalArgumentException("Unknown spell identifier in NBT: %s".formatted(comp.getString("Id")));
        }
        return spell.withNbt(comp);
    }

    protected Spell withNbt(NbtCompound comp) {
        return this;
    }

    public void writeToNbt(NbtCompound comp) {
        comp.putString("Id", this.id.toString());
    }

    public ImmutableList<SpellReaction> getForcedEffectReactions() {
        return ImmutableList.of();
    }

    public @NotNull SpellForm determineUseForm() {
        return Objects.requireNonNullElse(this.preferredUseForm, ModForms.RAY);
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public void startCast(ServerSpellCast cast, ServerWorld world, Entity caster) {
        for (var effect : this.effects) {
            effect.startCast(cast, world, caster);
        }
    }

    public void endCast(ServerSpellCast cast, ServerWorld world) {
        for (var effect : this.effects) {
            effect.endCast(cast, world);
        }
    }

    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        var passThrough = true;
        for (var effect : this.effects) {
            passThrough = effect.onSingleEntityHit(cast, entity) && passThrough;
        }
        return passThrough;
    }

    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        var passThrough = true;
        for (var effect : this.effects) {
            passThrough = passThrough && effect.onSingleBlockHit(cast, world, pos, dir);
        }
        return passThrough;
    }

    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        for (var effect : this.effects) {
            effect.onAreaHit(cast, world, box);
        }
    }
}
