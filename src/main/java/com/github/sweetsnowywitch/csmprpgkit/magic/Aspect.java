package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Aspect implements SpellElement, Comparable<Aspect> {
    public enum Kind {
        EFFECT,
        REACTION,
    }

    public static final int DEFAULT_COLOR = ColorHelper.Argb.getArgb(0xFF, 0x99, 0xD9, 0xEA); /* light blue, kinda */

    public final Identifier id;

    private final Kind kind;
    private final ImmutableMap<String, @NotNull Float> scales;
    private final int color;
    private final boolean primary;
    private final int order;
    private final Identifier texturePath;
    private final ImmutableList<SpellEffect> genericEffects;
    private final ImmutableList<SpellReaction> genericEffectReactions;

    public Aspect(Identifier id, Kind kind, ImmutableMap<String, @NotNull Float> scales,
                  int color, boolean primary, int order,
                  ImmutableList<SpellEffect> genericEffects, ImmutableList<SpellReaction> genericEffectReactions) {
        this.id = id;
        this.kind = kind;
        this.scales = scales;
        this.color = color;
        this.primary = primary;
        this.order = order;
        this.texturePath = new Identifier(id.getNamespace(), "textures/magic/aspects/"+id.getPath()+".png");
        this.genericEffects = genericEffects;
        this.genericEffectReactions = genericEffectReactions;
    }

    public Kind getKind() {
        return kind;
    }

    public float getBaseCost(String key) {
        return Objects.requireNonNull(scales.getOrDefault(key, (float)0));
    }

    public int getColor() {
        return this.color;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    @Override
    public List<SpellReaction> getGenericReactions() {
        return this.genericEffectReactions;
    }

    @Override
    public ImmutableList<SpellEffect> getGenericEffects() {
        return genericEffects;
    }

    public Identifier getTexturePath() {
        return this.texturePath;
    }

    @Override
    public String toString() {
        return "Aspect[id=" + id + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aspect aspect = (Aspect) o;
        return id.equals(aspect.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(@NotNull Aspect o) {
        var ord = this.order - o.order;
        if (ord == 0) {
            return this.id.compareTo(o.id);
        }
        return ord;
    }

    public static Aspect fromNbt(NbtCompound comp) {
        var id = Identifier.tryParse(comp.getString("Id"));
        if (id == null) {
            throw new IllegalStateException("Malformed aspect ID in NBT: %s".formatted(comp.getString("Id")));
        }
        return ModRegistries.ASPECTS.get(id);
    }

    @Override
    public void writeToNbt(NbtCompound comp) {
        comp.putString("Type", "Aspect");
        comp.putString("Id", this.id.toString());
    }
}
