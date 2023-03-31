package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Spell {
    public static final Spell EMPTY = new Spell(ImmutableList.of());
    private final ImmutableList<SpellEffect> effects;

    public Spell(ImmutableList<SpellEffect> effects) {
        this.effects = effects;
    }

    public ImmutableList<SpellEffect> getEffects() {
        return effects;
    }

    @Override
    public String toString() {
        var id = ModRegistries.SPELLS.inverse().get(this);
        if (id == null) {
            throw new IllegalStateException("toString called for unregistered spell");
        }
        return id.toString();
    }

    public void startCast(SpellCast cast, ImmutableList<SpellReaction> reactions) {
        for (var effect : this.effects) {
            effect.startCast(cast, reactions);
        }
    }

    public void endCast(SpellCast cast, ImmutableList<SpellReaction> reactions) {
        for (var effect : this.effects) {
            effect.endCast(cast, reactions);
        }
    }

    public void onSingleEntityHit(SpellCast cast, Entity entity, ImmutableList<SpellReaction> reactions) {
        for (var effect : this.effects) {
            effect.onSingleEntityHit(cast, entity, reactions);
        }
    }

    public void onSingleBlockHit(SpellCast cast, BlockPos pos, Direction dir, ImmutableList<SpellReaction> reactions) {
        for (var effect : this.effects) {
            effect.onSingleBlockHit(cast, pos, dir, reactions);
        }
    }

    public void onAreaHit(SpellCast cast, Vec3d position, ImmutableList<SpellReaction> reactions) {
        for (var effect : this.effects) {
            effect.onAreaHit(cast, position, reactions);
        }
    }

    public void onSelfHit(SpellCast cast, ImmutableList<SpellReaction> reactions) {
        for (var effect : this.effects) {
            effect.onSelfHit(cast, reactions);
        }
    }
}
