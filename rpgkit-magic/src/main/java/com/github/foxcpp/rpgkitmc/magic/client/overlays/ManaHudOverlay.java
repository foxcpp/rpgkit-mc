package com.github.foxcpp.rpgkitmc.magic.client.overlays;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class ManaHudOverlay implements HudRenderCallback {
    private static final Identifier EMPTY_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_bar.png");
    private static final Identifier ZERO_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_0.png");
    private static final Identifier ONE_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_1.png");
    private static final Identifier TWO_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_2.png");
    private static final Identifier THREE_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_3.png");
    private static final Identifier FOUR_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_4.png");
    private static final Identifier FIVE_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_5.png");
    private static final Identifier SIX_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_6.png");
    private static final Identifier SEVEN_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_7.png");
    private static final Identifier EIGHT_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_8.png");
    private static final Identifier NINE_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_9.png");
    private static final Identifier TEN_MANA = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/mana/mana_10.png");

    @Override
    public void onHudRender(DrawContext ctx, float tickDelta) {
        var x = 0;
        var y = 0;
        var client = MinecraftClient.getInstance();

        assert client != null;
        assert client.player != null;
        assert client.interactionManager != null;
        var manaComponent = client.player.getComponent(ModComponents.MANA);
        if (manaComponent.getMaxValue() == 0) {
            return;
        }

        if (client.interactionManager.getCurrentGameMode().equals(GameMode.SURVIVAL) ||
                client.interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {

            var width = client.getWindow().getScaledWidth();
            var height = client.getWindow().getScaledHeight();
            x = width / 2;
            y = height;

            // FIXME: possibly redundant since 1.20
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.setShaderTexture(0, EMPTY_MANA);
            ctx.drawTexture(EMPTY_MANA, x - 118, y - 60, 0, 0, 32, 64,
                    32, 64);

            Identifier segmentTexture;
            switch ((int) ((float) manaComponent.getValue() / manaComponent.getMaxValue() * 10)) {
                case 0 -> segmentTexture = ZERO_MANA;
                case 1 -> segmentTexture = ONE_MANA;
                case 2 -> segmentTexture = TWO_MANA;
                case 3 -> segmentTexture = THREE_MANA;
                case 4 -> segmentTexture = FOUR_MANA;
                case 5 -> segmentTexture = FIVE_MANA;
                case 6 -> segmentTexture = SIX_MANA;
                case 7 -> segmentTexture = SEVEN_MANA;
                case 8 -> segmentTexture = EIGHT_MANA;
                case 9 -> segmentTexture = NINE_MANA;
                case 10 -> segmentTexture = TEN_MANA;
                default -> segmentTexture = EMPTY_MANA;
            }

            ctx.drawTexture(segmentTexture, x - 118, y - 60, 0, 0, 32, 64,
                    32, 64);
        }
    }
}