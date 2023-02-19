package com.github.sweetsnowywitch.csmprpgkit.client.overlays;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ManaHudOverlay implements HudRenderCallback {
    private static final Identifier FILLED_MANA = new Identifier(RPGKitMod.MOD_ID,
            "textures/hud/mana/filled_mana.png");
    private static final Identifier EMPTY_MANA = new Identifier(RPGKitMod.MOD_ID,
            "textures/hud/mana/empty_mana.png");

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        var x = 0;
        var y = 0;
        var client = MinecraftClient.getInstance();
        if (client != null) {
            var width = client.getWindow().getScaledWidth();
            var height = client.getWindow().getScaledHeight();
            x = width / 2;
            y = height;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, EMPTY_MANA);
        for(int i = 0; i < client.player.getComponent(ModComponents.MANA).getMaxValue(); i++) {
            DrawableHelper.drawTexture(matrixStack,x - 94 + (i * 9),y - 54,0,0,12,12,
                    12,12);
        }

        RenderSystem.setShaderTexture(0, FILLED_MANA);
        for(var i = 0; i < client.player.getComponent(ModComponents.MANA).getValue(); i++) {
                DrawableHelper.drawTexture(matrixStack,x - 94 + (i * 9),y - 54,0,0,12,12,
                        12,12);
        }
    }
}