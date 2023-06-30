package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class SpellForm {
    private final ImmutableMap<String, Float> costMultipliers;
    private final ImmutableMap<String, Float> costTerms;

    public SpellForm(ImmutableMap<String, Float> costMultipliers, ImmutableMap<String, Float> costTerms) {
        this.costMultipliers = costMultipliers;
        this.costTerms = costTerms;
    }

    public float getCostMultiplier(String key) {
        return Objects.requireNonNull(costMultipliers.getOrDefault(key, (float) 1));
    }

    public float getCostTerm(String key) {
        return Objects.requireNonNull(costTerms.getOrDefault(key, (float) 0));
    }

    public float applyCost(String key, float val) {
        return val * this.getCostMultiplier(key) + this.getCostTerm(key);
    }

    /**
     * startCast is called when spell is just cast. It should call startCast
     * on all effects (e.g. super.startCast) and do other necessary initialization.
     *
     * @param cast   SpellCast object containing all info about how spell is cast.
     * @param world  Logical server world where cast is happening.
     * @param caster Entity that performs the cast, does not have to be LivingEntity.
     */
    @MustBeInvokedByOverriders
    public void startCast(@NotNull ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {

    }

    /**
     * Called when spell is interrupted/dissolved via external means.
     *
     * @param cast  SpellCast object containing all info about how spell is cast.
     * @param world Logical server world where cast is happening.
     */
    @MustBeInvokedByOverriders
    public void endCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world) {
        
    }

    public String toString() {
        var id = MagicRegistries.FORMS.getId(this);
        if (id == null) {
            throw new IllegalStateException("toString called to unregistered SpellForm");
        }
        return id.toString();
    }
}
