package com.github.sweetsnowywitch.csmprpgkit.magic.effects.use;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.MagicArea;
import com.github.sweetsnowywitch.csmprpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.use.special.MuteEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.use.special.WardEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class UseEffects {
    public static final UseEffect.JsonFactory POTION = PotionEffect::new;
    public static final UseEffect.JsonFactory DAMAGE = DamageEffect::new;
    public static final UseEffect.JsonFactory MUTE = MuteEffect::new;
    public static final UseEffect.JsonFactory PUSH = PushEffect::new;
    public static final UseEffect.JsonFactory WARD = WardEffect::new;
    public static final UseEffect.JsonFactory FIRE = FireEffect::new;
    public static final UseEffect.JsonFactory EXTINGUISH = ExtinguishEffect::new;
    public static final UseEffect.JsonFactory BONE_MEAL = BoneMealEffect::new;
    public static final UseEffect.JsonFactory EXPLOSION = ExplosionEffect::new;

    public static void register() {
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "potion"), POTION);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "damage"), DAMAGE);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "push"), PUSH);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "ward"), WARD);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "mute"), MUTE);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "fire"), FIRE);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "extinguish"), EXTINGUISH);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "bone_meal"), BONE_MEAL);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "explosion"), EXPLOSION);

        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMod.MOD_ID, "potion"), PotionEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMod.MOD_ID, "damage"), DamageEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMod.MOD_ID, "push"), PushEffect.Reaction::new);

        Registry.register(MagicRegistries.EFFECT_AREAS, new Identifier(RPGKitMod.MOD_ID, "ward"), MagicArea.factoryFor(WardEffect.Area::new));
        WardEffect.registerListener();
    }
}
