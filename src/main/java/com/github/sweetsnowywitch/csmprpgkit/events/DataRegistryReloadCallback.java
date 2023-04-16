package com.github.sweetsnowywitch.csmprpgkit.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface DataRegistryReloadCallback {
    Event<DataRegistryReloadCallback> EVENT = EventFactory.createArrayBacked(DataRegistryReloadCallback.class,
            (listeners) -> () -> {
                for (DataRegistryReloadCallback listener : listeners) {
                    ActionResult result = listener.onReloaded();

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });
    ActionResult onReloaded();
}
