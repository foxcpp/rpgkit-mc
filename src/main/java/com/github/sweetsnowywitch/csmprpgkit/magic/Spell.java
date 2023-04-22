package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Objects;

public class Spell {
    public final Identifier id;
    public static final Spell EMPTY = new Spell(Identifier.of(RPGKitMod.MOD_ID, "empty"), ImmutableList.of());
    private final ImmutableList<SpellEffect> effects;
    public Spell(Identifier id, ImmutableList<SpellEffect> effects) {
        this.id = id;
        this.effects = effects;
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

    public void onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        for (var effect : this.effects) {
            effect.onSingleEntityHit(cast, entity);
        }
    }

    public void onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        for (var effect : this.effects) {
            effect.onSingleBlockHit(cast, world, pos, dir);
        }
    }

    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        for (var effect : this.effects) {
            effect.onAreaHit(cast, world, box);
        }
    }
}
