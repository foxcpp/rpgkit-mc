package com.github.sweetsnowywitch.rpgkit.components;

import com.github.sweetsnowywitch.rpgkit.RPGKitMod;
import com.github.sweetsnowywitch.rpgkit.components.entity.AbilitiesComponent;
import com.github.sweetsnowywitch.rpgkit.components.entity.ClassComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<AbilitiesComponent> ABILITIES =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMod.MOD_ID, "abilities"), AbilitiesComponent.class);
    public static final ComponentKey<ClassComponent> CLASS =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMod.MOD_ID, "class"), ClassComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // XXX: Не забывай вписывать компоненты в fabric.mod.json.
        registry.registerForPlayers(ABILITIES, AbilitiesComponent::new, RespawnCopyStrategy.CHARACTER);
        registry.registerForPlayers(CLASS, ClassComponent::new, RespawnCopyStrategy.CHARACTER);
    }
}
