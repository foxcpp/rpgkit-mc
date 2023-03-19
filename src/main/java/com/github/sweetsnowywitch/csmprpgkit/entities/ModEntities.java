package com.github.sweetsnowywitch.csmprpgkit.entities;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<SpellRayEntity> SPELL_RAY = FabricEntityTypeBuilder.create(SpawnGroup.MISC,
                    SpellRayEntity::empty).
            dimensions(EntityDimensions.changing(0.42f, 0.42f)).build();

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE, new Identifier(RPGKitMod.MOD_ID, "spell_ray"), SPELL_RAY);
    }
}
