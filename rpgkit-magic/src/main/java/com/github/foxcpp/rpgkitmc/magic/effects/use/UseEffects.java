package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.MagicArea;
import com.github.foxcpp.rpgkitmc.magic.effects.use.special.MuteEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.use.special.WardEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class UseEffects {
    public static void register() {
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "potion"), PotionEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "damage"), DamageEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "push"), PushEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "ward"), WardEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "mute"), MuteEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "fire"), FireEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "extinguish"), ExtinguishEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "bone_meal"), BoneMealEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "explosion"), ExplosionEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "place_block"), PlaceBlockEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "break_block"), BreakBlockEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "pick_up_item"), PickUpItemEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "spawn_entity"), SpawnEntityEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "fall_block"), FallBlockEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "heal"), HealEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "insert_item"), InsertItemEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "transfer_mana"), TransferManaEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "place_fluid"), PlaceFluidEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "transmute_block"), TransmuteBlockEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "area_spread"), AreaSpreadEffect::new);
        Registry.register(MagicRegistries.USE_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "freeze"), FreezeEffect::new);

        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "potion"), PotionEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "damage"), DamageEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "push"), PushEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "ward"), WardEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "explosion"), ExplosionEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "break_block"), BreakBlockEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "fall_block"), FallBlockEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "heal"), HealEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "transfer_mana"), TransferManaEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "area_spread"), AreaSpreadEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "freeze"), FreezeEffect.Reaction::new);

        Registry.register(MagicRegistries.EFFECT_AREAS, new Identifier(RPGKitMagicMod.MOD_ID, "ward"), MagicArea.factoryFor(WardEffect.Area::new));
        WardEffect.registerListener();
    }
}
