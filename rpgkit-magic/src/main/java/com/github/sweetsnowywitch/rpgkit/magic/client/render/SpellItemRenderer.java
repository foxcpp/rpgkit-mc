package com.github.sweetsnowywitch.rpgkit.magic.client.render;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.items.SpellItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

@Environment(EnvType.CLIENT)
public class SpellItemRenderer extends GeoItemRenderer<SpellItem> {
    public static class Model extends AnimatedGeoModel<SpellItem> {
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
    public Color getRenderColor(SpellItem animatable, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight) {
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
