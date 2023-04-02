package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
