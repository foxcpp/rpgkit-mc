package com.github.sweetsnowywitch.csmprpgkit.client.render;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ModRenderers {
    public static void register() {
        EntityRendererRegistry.register(ModEntities.SPELL_RAY, SpellRayRenderer::new);
        //EntityModelLayerRegistry.registerModelLayer(IcicleModel.ICILE, IcicleModel::getTexturedModelData);
    }
}
