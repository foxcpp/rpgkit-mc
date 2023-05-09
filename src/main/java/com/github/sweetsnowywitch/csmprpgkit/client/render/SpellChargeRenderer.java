package com.github.sweetsnowywitch.csmprpgkit.client.render;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.client.model.SpellChargeModel;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellChargeEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

@Environment(EnvType.CLIENT)
public class SpellChargeRenderer extends EntityRenderer<SpellChargeEntity> {
    private final EntityModel<SpellChargeEntity> model;

    public SpellChargeRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager);
        this.model = new SpellChargeModel(renderManager.getPart(SpellChargeModel.MAIN));
    }

    @Override
    public void render(SpellChargeEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.push();
        matrices.translate(0.0f, -1.3f, 0.0f);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(this.getTexture(entity)));

        this.model.render(matrices, vertexConsumer, light, 0,
                ColorHelper.Argb.getRed(entity.baseColor) / 255f,
                ColorHelper.Argb.getGreen(entity.baseColor) / 255f,
                ColorHelper.Argb.getBlue(entity.baseColor) / 255f, 1.0f);
        matrices.pop();

    }

    @Override
    public Identifier getTexture(SpellChargeEntity entity) {
        return new Identifier(RPGKitMod.MOD_ID, "textures/entity/spell_charge.png");
    }
}
