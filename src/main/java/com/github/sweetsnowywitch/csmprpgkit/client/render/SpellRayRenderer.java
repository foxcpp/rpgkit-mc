package com.github.sweetsnowywitch.csmprpgkit.client.render;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellRayEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SpellRayRenderer extends EntityRenderer<SpellRayEntity> {
    public SpellRayRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(SpellRayEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        var radius = 0.075f;
        var height = entity.getLength(tickDelta);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getYaw(tickDelta)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(entity.getPitch(tickDelta)));
        //matrices.translate(0, entity.getDimensions(entity.getPose()).height * 0.5F, 0);

        //var ageFactor = 1 - ((float)entity.age) / entity.maxAge;
        var ageFactor = 1;

        var buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(this.getTexture(entity)));

        var entry = matrices.peek();
        var position = entry.getPositionMatrix();
        var normal = entry.getNormalMatrix();

        renderBeamFace(position, normal, buffer, height, 0, 128*ageFactor, -radius, -radius, -radius, radius);
        renderBeamFace(position, normal, buffer, height, 0, 128*ageFactor, -radius, radius, radius, radius);
        renderBeamFace(position, normal, buffer, height, 0, 128*ageFactor, radius, radius, radius, -radius);
        renderBeamFace(position, normal, buffer, height, 0, 128*ageFactor, radius, -radius, -radius, -radius);

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderBeamFace(Matrix4f position, Matrix3f normal, VertexConsumer buffer,
                                float height, float yOffset, float alpha,
                                float x1, float z1,
                                float x2, float z2) {
        buffer.
                vertex(position, x1, yOffset, z1).
                color(255, 255, 255, 128).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();
        buffer.
                vertex(position, x1, yOffset+height, z1).
                color(255, 255, 255, 128).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();
        buffer.
                vertex(position, x2, yOffset+height, z2).
                color(255, 255, 255, 128).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();
        buffer.
                vertex(position, x2, yOffset, z2).
                color(255, 255, 255, 128).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();

    }

    @Override
    public Identifier getTexture(SpellRayEntity entity) {
        return Identifier.of(RPGKitMod.MOD_ID, "textures/entity/spell_ray.png");
    }
}
