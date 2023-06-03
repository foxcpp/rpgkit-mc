package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellArea;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEffects {
    public static final SpellEffect.Factory POTION = SpellEffect.factoryFor(PotionEffect::new, PotionEffect::new);
    public static final SpellEffect.Factory DAMAGE = SpellEffect.factoryFor(DamageEffect::new, DamageEffect::new);
    public static final SpellEffect.Factory MUTE = SpellEffect.factoryFor(MuteEffect::new, MuteEffect::new);
    public static final SpellEffect.Factory PUSH_AWAY = SpellEffect.factoryFor(PushAwayEffect::new, PushAwayEffect::new);
    public static final SpellEffect.Factory WARD = SpellEffect.factoryFor(WardEffect::new, WardEffect::new);
    public static final SpellEffect.Factory FIRE = SpellEffect.factoryFor(FireEffect::new, FireEffect::new);
    public static final SpellEffect.Factory EXTINGUISH = SpellEffect.factoryFor(ExtinguishEffect::new, ExtinguishEffect::new);
    public static final SpellEffect.Factory BONE_MEAL = SpellEffect.factoryFor(BoneMealEffect::new, BoneMealEffect::new);
    public static final SpellEffect.Factory EXPLOSION = SpellEffect.factoryFor(ExplosionEffect::new, ExplosionEffect::new);

    public static void register() {
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "potion"), POTION);
        Registry.register(ModRegistries.SPELL_EFFECT_REACTIONS, new Identifier(RPGKitMod.MOD_ID, "potion"),
                SpellReaction.factoryFor(PotionEffect.Reaction::new, PotionEffect.Reaction::new));

        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "damage"), DAMAGE);
        Registry.register(ModRegistries.SPELL_EFFECT_REACTIONS, new Identifier(RPGKitMod.MOD_ID, "damage"),
                SpellReaction.factoryFor(DamageEffect.Reaction::new, DamageEffect.Reaction::new));

        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "push_away"), PUSH_AWAY);
        Registry.register(ModRegistries.SPELL_EFFECT_REACTIONS, new Identifier(RPGKitMod.MOD_ID, "push_away"),
                SpellReaction.factoryFor(PushAwayEffect.Reaction::new, PushAwayEffect.Reaction::new));

        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "ward"), WARD);
        Registry.register(ModRegistries.SPELL_EFFECT_AREAS, new Identifier(RPGKitMod.MOD_ID, "ward"),
                SpellArea.factoryFor(WardEffect.Area::new));
        WardEffect.registerListener();

        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "mute"), MUTE);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "fire"), FIRE);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "extinguish"), EXTINGUISH);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "bone_meal"), BONE_MEAL);
        Registry.register(ModRegistries.SPELL_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "explosion"), EXPLOSION);
    }
}
