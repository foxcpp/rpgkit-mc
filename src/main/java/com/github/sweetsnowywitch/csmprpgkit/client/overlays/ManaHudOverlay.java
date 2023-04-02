package com.github.sweetsnowywitch.csmprpgkit.client.overlays;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class ManaHudOverlay implements HudRenderCallback {
    private static final Identifier EMPTY_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_bar.png");
    private static final Identifier ZERO_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_0.png");
    private static final Identifier ONE_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_1.png");
    private static final Identifier TWO_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_2.png");
    private static final Identifier THREE_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_3.png");
    private static final Identifier FOUR_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_4.png");
    private static final Identifier FIVE_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_5.png");
    private static final Identifier SIX_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_6.png");
    private static final Identifier SEVEN_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_7.png");
    private static final Identifier EIGHT_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_8.png");
    private static final Identifier NINE_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_9.png");
    private static final Identifier TEN_MANA = new Identifier(RPGKitMod.MOD_ID, "textures/hud/mana/mana_10.png");

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta) {
        var x = 0;
        var y = 0;
        var client = MinecraftClient.getInstance();

        assert client != null;
        assert client.player != null;
        assert client.interactionManager != null;
        if (client.interactionManager.getCurrentGameMode().equals(GameMode.SURVIVAL) ||
                client.interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {

            var width = client.getWindow().getScaledWidth();
            var height = client.getWindow().getScaledHeight();
            x = width / 2;
            y = height;

            var manaComponent = client.player.getComponent(ModComponents.MANA);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.setShaderTexture(0, EMPTY_MANA);
            DrawableHelper.drawTexture(matrixStack, x - 118, y - 60, 0, 0, 32, 64,
                    32, 64);

            switch ((int)((float) manaComponent.getValue() / manaComponent.getMaxValue() * 10)) {
                case 0 -> RenderSystem.setShaderTexture(0, ZERO_MANA);
                case 1 -> RenderSystem.setShaderTexture(0, ONE_MANA);
                case 2 -> RenderSystem.setShaderTexture(0, TWO_MANA);
                case 3 -> RenderSystem.setShaderTexture(0, THREE_MANA);
                case 4 -> RenderSystem.setShaderTexture(0, FOUR_MANA);
                case 5 -> RenderSystem.setShaderTexture(0, FIVE_MANA);
                case 6 -> RenderSystem.setShaderTexture(0, SIX_MANA);
                case 7 -> RenderSystem.setShaderTexture(0, SEVEN_MANA);
                case 8 -> RenderSystem.setShaderTexture(0, EIGHT_MANA);
                case 9 -> RenderSystem.setShaderTexture(0, NINE_MANA);
                case 10 -> RenderSystem.setShaderTexture(0, TEN_MANA);
            }

            if (manaComponent.getValue() == 0)
                RenderSystem.setShaderTexture(0, EMPTY_MANA);
            DrawableHelper.drawTexture(matrixStack, x - 118, y - 60, 0, 0, 32, 64,
                    32, 64);
        }
    }
}