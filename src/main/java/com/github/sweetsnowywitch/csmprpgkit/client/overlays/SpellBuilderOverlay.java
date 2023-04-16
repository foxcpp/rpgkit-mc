package com.github.sweetsnowywitch.csmprpgkit.client.overlays;

import com.github.sweetsnowywitch.csmprpgkit.client.ClientSpellBuildHandler;
import com.github.sweetsnowywitch.csmprpgkit.magic.Aspect;
import com.github.sweetsnowywitch.csmprpgkit.magic.ItemElement;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public class SpellBuilderOverlay implements HudRenderCallback {
    public ClientSpellBuildHandler handler;

    public SpellBuilderOverlay(ClientSpellBuildHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onHudRender(MatrixStack matrix, float tickDelta) {
        if (handler.getBuilder() == null) {
            return;
        }

        var client = MinecraftClient.getInstance();
        var width = client.getWindow().getScaledWidth();
        var height = client.getWindow().getScaledHeight();

        // Available aspects.

        var totalAspects = 0;
        var aspects = handler.getPrimaryEffectAspects();
        for (var asp : aspects) {
            if (!asp.isPrimary() || asp.getKind() != Aspect.Kind.EFFECT) {
                continue;
            }
            totalAspects++;
        }

        var drawnAspects = 0;
        for (var asp : aspects) {
            var x = width/2 - totalAspects*18/2 + drawnAspects*18;
            var y = height - 70;

            this.drawElement(matrix, x, y, asp);

            drawnAspects++;
        }

        // Pending elements.
        drawnAspects = 0;
        var pending = handler.getBuilder().getPendingElements();
        for (var asp : pending) {
            var x = width / 2 - 90 + 18 * drawnAspects;
            var y = height - 70 + 18;

            this.drawElement(matrix, x, y, asp);

            drawnAspects++;
        }
    }

    public void drawElement(MatrixStack matrixStack, int x, int y, SpellElement element) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        if (element instanceof Aspect asp) {
            RenderSystem.setShaderTexture(0, asp.getTexturePath());
        } else if (element instanceof ItemElement.Stack ies) {
            var renderer = MinecraftClient.getInstance().getItemRenderer();
            renderer.renderInGui(ies.getStack(), x, y);
        } else if (element instanceof ItemElement ie) {
            var renderer = MinecraftClient.getInstance().getItemRenderer();
            renderer.renderInGui(new ItemStack(ie.getItem()), x, y);
        }

        // Setting correct texture width/height does not matter if we use u=0,v=0.
        DrawableHelper.drawTexture(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
    }
}
