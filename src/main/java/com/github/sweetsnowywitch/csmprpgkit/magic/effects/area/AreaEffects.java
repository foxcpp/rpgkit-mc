package com.github.sweetsnowywitch.csmprpgkit.magic.effects.area;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.AreaEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class AreaEffects {
    public static AreaEffect.JsonFactory SURFACE_SPRAY = SurfaceSprayEffect::new;
    public static AreaEffect.JsonFactory PERSISTENT = PersistentAreaEffect::new;

    public static void register() {
        Registry.register(MagicRegistries.AREA_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "surface_spray"), SURFACE_SPRAY);
        Registry.register(MagicRegistries.AREA_EFFECTS, new Identifier(RPGKitMod.MOD_ID, "persistent_area"), PERSISTENT);

        Registry.register(MagicRegistries.REACTIONS, new Identifier(RPGKitMod.MOD_ID, "surface_spray"), SurfaceSprayEffect.Reaction::new);
    }
}
