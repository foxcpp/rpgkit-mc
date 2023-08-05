package com.github.foxcpp.rpgkitmc.magic.client.render;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.items.SpellItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class SpellItemRenderer extends GeoItemRenderer<SpellItem> {
    public static class Model extends GeoModel<SpellItem> {
        @Override
        public Identifier getModelResource(SpellItem object) {
            return Identifier.of(RPGKitMagicMod.MOD_ID, "geo/item/spell.geo.json");
        }

        @Override
        public Identifier getTextureResource(SpellItem object) {
            return Identifier.of(RPGKitMagicMod.MOD_ID, "textures/item/spell.png");

        }

        @Override
        public Identifier getAnimationResource(SpellItem animatable) {
            return Identifier.of(RPGKitMagicMod.MOD_ID, "animations/spell.animation.json");
        }
    }

    @Override
    public Color getRenderColor(SpellItem animatable, float partialTick, int packedLight) {
        var nbt = this.currentItemStack.getNbt();
        if (nbt == null) {
            return Color.WHITE;
        }
        return Color.ofOpaque(nbt.getInt("Color"));
    }

    public SpellItemRenderer() {
        super(new Model());
    }
}
