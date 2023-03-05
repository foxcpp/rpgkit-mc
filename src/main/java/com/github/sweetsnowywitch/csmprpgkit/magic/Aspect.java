package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Aspect implements SpellElement {
    public enum Kind {
        EFFECT,
        REACTION,
    }

    private final Kind kind;
    private final ImmutableMap<String, @NotNull Float> scales;

    public Aspect(Kind kind, ImmutableMap<String, @NotNull Float> scales) {
        this.kind = kind;
        this.scales = scales;
    }

    public Kind getKind() {
        return kind;
    }

    public float getBaseCost(String key) {
        return Objects.requireNonNull(scales.getOrDefault(key, (float)0));
    }

    @Override
    public String toString() {
        var id = ModRegistries.ASPECTS.inverse().get(this);
        if (id == null) {
            throw new IllegalStateException("toString called for unregistered aspect");
        }
        return id.toString();
    }
}
