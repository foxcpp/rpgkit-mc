package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {
    public static final EntityType<SpellRayEntity> SPELL_RAY = FabricEntityTypeBuilder.create(SpawnGroup.MISC,
                    SpellRayEntity::empty).
            dimensions(EntityDimensions.changing(0.42f, 0.42f)).build();
    public static final EntityType<SoundBarrierEntity> SOUND_BARRIER = FabricEntityTypeBuilder.create(SpawnGroup.MISC,
                    SoundBarrierEntity::empty).build();

    public static void register() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMod.MOD_ID, "spell_ray"), SPELL_RAY);

        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMod.MOD_ID, "sound_barrier"), SOUND_BARRIER);
    }
}
