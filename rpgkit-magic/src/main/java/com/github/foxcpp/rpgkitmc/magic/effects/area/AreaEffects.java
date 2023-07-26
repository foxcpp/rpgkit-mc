package com.github.foxcpp.rpgkitmc.magic.effects.area;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AreaEffects {
    public static void register() {
        Registry.register(MagicRegistries.AREA_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "surface_spray"), SurfaceSprayEffect::new);
        Registry.register(MagicRegistries.AREA_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "persistent_area"), PersistentAreaEffect::new);
        Registry.register(MagicRegistries.AREA_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "forcefield"), ForcefieldEffect::new);
        Registry.register(MagicRegistries.AREA_EFFECTS, new Identifier(RPGKitMagicMod.MOD_ID, "lingering_potion"), LingeringPotionEffect::new);

        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "surface_spray"), SurfaceSprayEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "persistent_area"), PersistentAreaEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "forcefield"), ForcefieldEffect.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMagicMod.MOD_ID, "lingering_potion"), LingeringPotionEffect.Reaction::new);
    }
}
