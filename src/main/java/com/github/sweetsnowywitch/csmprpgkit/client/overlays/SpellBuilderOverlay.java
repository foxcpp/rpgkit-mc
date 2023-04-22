package com.github.sweetsnowywitch.csmprpgkit.client.overlays;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
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
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SpellBuilderOverlay implements HudRenderCallback {
    private static final Identifier FRAME_TEXTURE = new Identifier(RPGKitMod.MOD_ID, "textures/hud/frame.png");
    private static final int ELEMENT_SLOT_SIZE = 22;
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

        var guiStartHeight = height - 90;
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.isCreative()) {
            guiStartHeight += 20;
        }

        // Available aspects.
        var totalAspects = 0;
        var aspects = handler.getPrimaryEffectAspects();
        for (var asp : aspects) {
            if (!asp.isPrimary() || asp.getKind() != Aspect.Kind.EFFECT) {
                continue;
            }
            totalAspects++;
        }



        // Pending elements.
        var maxElements = handler.getBuilder().getMaxElements();
        var elementGap = (180 - ELEMENT_SLOT_SIZE*maxElements)/(maxElements - 1);

        var drawnAspects = 0;
        var pending = handler.getBuilder().getPendingElements();
        for (int i = 0; i < maxElements; i++) {
            var x = width / 2 - 90 + (22 + elementGap) * drawnAspects;
            var y = guiStartHeight - 5;

            SpellElement element = null;
            if (i < pending.size()) {
                element = pending.get(i);
            }

            this.drawElement(matrix, x, y, element, 1);

            drawnAspects++;
        }

        drawnAspects = 0;
        for (var asp : aspects) {
            var x = width/2 - totalAspects*18/2 + drawnAspects*18;
            var y = guiStartHeight + ELEMENT_SLOT_SIZE;

            this.drawElement(matrix, x, y, asp, 0.75f);

            drawnAspects++;
        }
    }

    public void drawElement(MatrixStack matrixStack, int x, int y, @Nullable SpellElement element, float scale) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.setShaderTexture(0, FRAME_TEXTURE);
        DrawableHelper.drawTexture(matrixStack, x, y, 0, 0,
                (int)(ELEMENT_SLOT_SIZE*scale), (int)(ELEMENT_SLOT_SIZE*scale),
                (int)(ELEMENT_SLOT_SIZE*scale), (int)(ELEMENT_SLOT_SIZE*scale));
        var frameOffset = (ELEMENT_SLOT_SIZE - 16)/2;

        if (element == null) {
            return;
        }

        if (element instanceof Aspect asp) {
            RenderSystem.setShaderTexture(0, asp.getTexturePath());
        } else if (element instanceof ItemElement.Stack ies) {
            var renderer = MinecraftClient.getInstance().getItemRenderer();
            renderer.renderInGui(ies.getStack(), x, y);
        } else if (element instanceof ItemElement ie) {
            var renderer = MinecraftClient.getInstance().getItemRenderer();
            renderer.renderInGui(new ItemStack(ie.getItem()), x, y);
        }

        DrawableHelper.drawTexture(matrixStack, x+(int)(frameOffset*scale), y+(int)(frameOffset*scale), 0, 0,
                (int)(16*scale), (int)(16*scale), (int)(16*scale), (int)(16*scale));
    }
}
