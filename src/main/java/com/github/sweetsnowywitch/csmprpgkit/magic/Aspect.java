package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.google.common.collect.ImmutableMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Aspect implements SpellElement {
    public enum Kind {
        EFFECT,
        REACTION,
    }

    public static final int DEFAULT_COLOR = ColorHelper.Argb.getArgb(0xFF, 0x99, 0xD9, 0xEA); /* light blue, kinda */

    public final Identifier id;

    private final Kind kind;
    private final ImmutableMap<String, @NotNull Float> scales;
    private final int color;

    public Aspect(Identifier id, Kind kind, ImmutableMap<String, @NotNull Float> scales, int color) {
        this.id = id;
        this.kind = kind;
        this.scales = scales;
        this.color = color;
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
}
