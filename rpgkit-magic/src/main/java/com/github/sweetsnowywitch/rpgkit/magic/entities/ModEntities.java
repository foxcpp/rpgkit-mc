package com.github.sweetsnowywitch.rpgkit.magic.entities;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
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
    public static final EntityType<PersistentMagicEntity> PERSISTENT_MAGIC = FabricEntityTypeBuilder.create(SpawnGroup.MISC,
            PersistentMagicEntity::empty).build();
    public static final EntityType<ForcefieldEntity> FORCEFIELD = FabricEntityTypeBuilder.create(SpawnGroup.MISC,
            ForcefieldEntity::empty).build();
    public static final EntityType<SpellBlastEntity> SPELL_BLAST = FabricEntityTypeBuilder.create(SpawnGroup.MISC,
            SpellBlastEntity::empty).build();
    public static final EntityType<SpellChargeEntity> SPELL_CHARGE = FabricEntityTypeBuilder.create(SpawnGroup.MISC,
            SpellChargeEntity::empty).build();

    public static void register() {
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMagicMod.MOD_ID, "spell_ray"), SPELL_RAY);
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMagicMod.MOD_ID, "spell_blast"), SPELL_BLAST);
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMagicMod.MOD_ID, "spell_charge"), SPELL_CHARGE);
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMagicMod.MOD_ID, "sound_barrier"), SOUND_BARRIER);
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMagicMod.MOD_ID, "persistent_magic"), PERSISTENT_MAGIC);
        Registry.register(Registry.ENTITY_TYPE, new Identifier(RPGKitMagicMod.MOD_ID, "forcefield"), FORCEFIELD);
    }
}
