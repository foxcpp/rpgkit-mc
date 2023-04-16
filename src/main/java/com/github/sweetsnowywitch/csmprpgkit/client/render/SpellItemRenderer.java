package com.github.sweetsnowywitch.csmprpgkit.client.render;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.items.SpellItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class SpellItemRenderer extends GeoItemRenderer<SpellItem> {
    public static class Model extends AnimatedGeoModel<SpellItem> {
        @Override
        public Identifier getModelResource(SpellItem object) {
            return Identifier.of(RPGKitMod.MOD_ID, "geo/item/spell.geo.json");
        }

        @Override
        public Identifier getTextureResource(SpellItem object) {
            return Identifier.of(RPGKitMod.MOD_ID, "textures/item/spell.png");

        }

        @Override
        public Identifier getAnimationResource(SpellItem animatable) {
            return Identifier.of(RPGKitMod.MOD_ID, "animations/spell.animation.json");
        }
    }

    public SpellItemRenderer() {
        super(new Model());
    }
}
