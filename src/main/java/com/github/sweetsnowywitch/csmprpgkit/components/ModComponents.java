package com.github.sweetsnowywitch.csmprpgkit.components;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public final class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<AbilitiesComponent> ABILITIES =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMod.MOD_ID, "abilities"), AbilitiesComponent.class);
    public static final ComponentKey<ManaComponent> MANA =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMod.MOD_ID, "mana"), ManaComponent.class);

    public static final ComponentKey<ClassComponent> CLASS =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(RPGKitMod.MOD_ID, "class"), ClassComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // XXX: Не забывай вписывать компоненты в fabric.mod.json.
        registry.registerForPlayers(ABILITIES, AbilitiesComponent::new, RespawnCopyStrategy.CHARACTER);
        registry.registerForPlayers(MANA, ManaComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerForPlayers(CLASS, ClassComponent::new, RespawnCopyStrategy.CHARACTER);
    }
}
