package com.github.sweetsnowywitch.rpgkit.magic.components;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.components.chunk.MagicEffectsComponent;
import com.github.sweetsnowywitch.rpgkit.magic.components.entity.ActiveCastComponent;
import com.github.sweetsnowywitch.rpgkit.magic.components.entity.ManaComponent;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public final class ModComponents implements EntityComponentInitializer, ChunkComponentInitializer {
    // Entity
    public static final ComponentKey<ManaComponent> MANA =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMagicMod.MOD_ID, "mana"), ManaComponent.class);
    public static final ComponentKey<ActiveCastComponent> CAST =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMagicMod.MOD_ID, "cast"), ActiveCastComponent.class);
    // Chunk
    public static final ComponentKey<MagicEffectsComponent> CHUNK_MAGIC_EFFECTS =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMagicMod.MOD_ID, "chunk_magic_effects"), MagicEffectsComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // XXX: Не забывай вписывать компоненты в fabric.mod.json.
        registry.registerForPlayers(ABILITIES, AbilitiesComponent::new, RespawnCopyStrategy.CHARACTER);
        registry.registerForPlayers(MANA, ManaComponent::new, RespawnCopyStrategy.CHARACTER);
        registry.registerForPlayers(CLASS, ClassComponent::new, RespawnCopyStrategy.CHARACTER);
        registry.registerForPlayers(CAST, ActiveCastComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(CHUNK_MAGIC_EFFECTS, MagicEffectsComponent::new);
    }
}
