package com.github.sweetsnowywitch.csmprpgkit.components;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;

public final class ModComponents implements EntityComponentInitializer {
    //public static final ComponentKey<DndAbilitiesComponent> DND_SHEET =
    //   ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("fabricplayground", "dnd_sheet"), DndAbilitiesComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // XXX: Не забывай вписывать компоненты в fabric.mod.json.
        //registry.registerForPlayers(DND_SHEET, DndAbilitiesComponent::new, RespawnCopyStrategy.CHARACTER);
    }
}
