package com.github.foxcpp.rpgkitmc.magic.client.render;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.entities.SpellRayEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(EnvType.CLIENT)
public class SpellRayRenderer extends EntityRenderer<SpellRayEntity> {
    public SpellRayRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(SpellRayEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        var radius = 0.075f;
        var height = entity.getLength();

        matrices.push();

        // Align Y axis with rotation direction.
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(entity.getYaw(tickDelta)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((entity.getPitch(tickDelta))));

        var buffer = vertexConsumers.getBuffer(RenderLayer.getLightning());

        var argbStart = entity.rayBaseColor | 128 << 24;
        var argbEnd = entity.rayBaseColor | 128 << 24;

        renderBeam(matrices, buffer, argbStart, argbEnd,
                height, 0, 0,
                radius);

        argbStart |= 0xFF000000;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(15));

        renderBeam(matrices, buffer, argbStart, argbEnd,
                height, 0, 0, radius / 2);

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private void renderBeam(MatrixStack matrices, VertexConsumer buffer, int argbStart, int argbEnd,
                            float height, float centerX, float centerZ, float radius) {
        var entry = matrices.peek();
        var position = entry.getPositionMatrix();
        var normal = entry.getNormalMatrix();

        renderBeamFace(position, normal, buffer, height, 0, argbStart, argbEnd,
                centerX - radius, centerZ - radius, centerX - radius, centerZ + radius);
        renderBeamFace(position, normal, buffer, height, 0, argbStart, argbEnd,
                centerX - radius, centerZ + radius, centerX + radius, centerZ + radius);
        renderBeamFace(position, normal, buffer, height, 0, argbStart, argbEnd,
                centerX + radius, centerZ + radius, centerX + radius, centerZ - radius);
        renderBeamFace(position, normal, buffer, height, 0, argbStart, argbEnd,
                centerX + radius, centerZ - radius, centerX - radius, centerZ - radius);
    }

    private void renderBeamFace(Matrix4f position, Matrix3f normal, VertexConsumer buffer,
                                float height, float yOffset, int argbStart, int argbEnd,
                                float x1, float z1,
                                float x2, float z2) {
        buffer.
                vertex(position, x1, yOffset, z1).
                color(argbStart).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();
        buffer.
                vertex(position, x1, yOffset + height, z1).
                color(argbEnd).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();
        buffer.
                vertex(position, x2, yOffset + height, z2).
                color(argbEnd).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();
        buffer.
                vertex(position, x2, yOffset, z2).
                color(argbStart).
                texture(0, 1).
                overlay(OverlayTexture.DEFAULT_UV).
                light(15728880).
                normal(normal, 0, 1, 0).
                next();
    }

    @Override
    public Identifier getTexture(SpellRayEntity entity) {
        return Identifier.of(RPGKitMagicMod.MOD_ID, "textures/entity/spell_ray.png");
    }
}
