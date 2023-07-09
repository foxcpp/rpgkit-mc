package com.github.sweetsnowywitch.rpgkit.magic;

import com.github.sweetsnowywitch.rpgkit.magic.json.FloatModifier;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import org.jetbrains.annotations.NotNull;

/**
 * Checked by magical protection effects - including block wards, magic shields, etc.
 * to determine whether the protection is strong enough to reduce the effect
 * or protective spell is dissolved because the attack is too strong.
 * <p>
 * Strength is measured in non-negative flaot units.
 * <p>
 * For purposes of block breaking, value should be reduced to 0.
 */
public interface ProtectionBreakingEffect {
    @NotNull FloatModifier calculateEffectReduction(ServerSpellCast cast, float protectionStrength);

    boolean willDissolveProtection(ServerSpellCast cast, float protectionStrength);
}
