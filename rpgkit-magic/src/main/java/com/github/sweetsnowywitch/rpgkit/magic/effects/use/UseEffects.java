package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.MagicArea;
import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.use.special.MuteEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.use.special.WardEffect;
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
    public static final UseEffect.JsonFactory PLACE_BLOCK = PlaceBlockEffect::new;
    public static final UseEffect.JsonFactory BREAK_BLOCK = BreakBlockEffect::new;
    public static final UseEffect.JsonFactory PICK_UP_ITEM = PickUpItemEffect::new;

    public static void register() {
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "potion"), POTION);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "damage"), DAMAGE);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "push"), PUSH);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "ward"), WARD);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "mute"), MUTE);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "fire"), FIRE);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "extinguish"), EXTINGUISH);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "bone_meal"), BONE_MEAL);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "explosion"), EXPLOSION);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "place_block"), PLACE_BLOCK);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "break_block"), BREAK_BLOCK);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "pick_up_item"), PICK_UP_ITEM);

        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "potion"), PotionEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "damage"), DamageEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "push"), PushEffect.Reaction::new);

        Registry.register(MagicRegistries.EFFECT_AREAS, new Identifier(RPGKitMagicMod.MOD_ID, "ward"), MagicArea.factoryFor(WardEffect.Area::new));
        WardEffect.registerListener();
    }
}
