package com.github.foxcpp.rpgkitmc.magic.statuseffects;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModStatusEffects {
    public static final StatusEffect MUTE = new MuteStatusEffect();
    public static final StatusEffect SEALED = new SealedStatusEffect();
    public static final ManaRegenStatusEffect MANA_REGEN = new ManaRegenStatusEffect();
    public static final StatusEffect KNOCKBACK_RESISTANCE = (new KnockbackResistanceEffect()).addAttributeModifier(
            EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "0fff0d1e-1f0c-11ee-9b61-00155d11d75f",
            0.3, EntityAttributeModifier.Operation.ADDITION
    );

    public static void register() {
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMagicMod.MOD_ID, "sound_mute"), MUTE);
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMagicMod.MOD_ID, "sealed"), SEALED);
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMagicMod.MOD_ID, "mana_regen"), MANA_REGEN);
        Registry.register(Registry.STATUS_EFFECT, Identifier.of(RPGKitMagicMod.MOD_ID, "knockback_resistance"), KNOCKBACK_RESISTANCE);
    }
}
