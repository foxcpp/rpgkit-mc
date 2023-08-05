package com.github.foxcpp.rpgkitmc.magic.client.overlays;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import com.github.foxcpp.rpgkitmc.magic.client.ClientRPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.components.entity.ActiveCastComponent;
import com.github.foxcpp.rpgkitmc.magic.spell.Aspect;
import com.github.foxcpp.rpgkitmc.magic.spell.ItemElement;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ActiveCastOverlay implements HudRenderCallback {
    private static final Identifier FRAME_TEXTURE = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/frame.png");
    private static final Identifier CHANNEL_BAR_TEXTURE = new Identifier(RPGKitMagicMod.MOD_ID, "textures/hud/channel_bar.png");
    private static final int ELEMENT_SLOT_SIZE = 22;

    public ActiveCastOverlay() {
    }

    public void renderBuilder(ActiveCastComponent comp, DrawContext ctx, float tickDelta) {
        var client = MinecraftClient.getInstance();

        var width = client.getWindow().getScaledWidth();
        var height = client.getWindow().getScaledHeight();

        var matrix = ctx.getMatrices();

        var guiStartHeight = height - 90;
        if (client.player != null && client.player.isCreative()) {
            guiStartHeight += 20;
        }

        // Pending elements.
        {
            var maxElements = comp.getMaxElements();
            var elementGap = (180 - ELEMENT_SLOT_SIZE * maxElements) / (maxElements - 1);

            var drawnAspects = 0;
            var pending = comp.getPendingElements();
            for (int i = 0; i < maxElements; i++) {
                var x = width / 2 - 90 + (22 + elementGap) * drawnAspects;
                var y = guiStartHeight - 5;

                SpellElement element = null;
                if (i < pending.size()) {
                    element = pending.get(i);
                }

                this.drawElement(ctx, x, y, element, 1);

                drawnAspects++;
            }
        }

        // Bag icon if present.
        ItemStack bag = comp.getCatalystBag();
        if (bag != null) {
            var x = width / 2 - 18 * 3 - 2 * 18;
            var y = guiStartHeight + ELEMENT_SLOT_SIZE;

            ctx.drawItem(client.player, bag, x, y, 42);
        }

        // Available aspects.
        var elements = comp.getAvailableElements();
        {
            var drawnAspects = 0;
            if (elements.size() == 0) {
                var text = Text.translatable("rpgkit.magic.no_elements");
                var wid = client.textRenderer.getWidth(text);
                ctx.drawText(client.textRenderer, text, (width / 2 - wid / 2), (guiStartHeight + ELEMENT_SLOT_SIZE + 4),
                        0xFFFFFF, false);
            } else {
                for (var element : elements) {
                    var x = width / 2 - elements.size() * 18 / 2 + drawnAspects * 18;
                    var y = guiStartHeight + ELEMENT_SLOT_SIZE;

                    this.drawElement(ctx, x, y, element, 0.75f);
                    drawnAspects++;
                }
            }
        }

        // Text hints.
        {
            matrix.push();
            matrix.scale(0.5f, 0.5f, 0.5f);

            if (bag != null) {
                var x = width / 2 - 18 * 3 - 2 * 18 + 16 / 4;
                var y = guiStartHeight + ELEMENT_SLOT_SIZE + 17;

                var provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                client.textRenderer.drawWithOutline(ClientRPGKitMagicMod.SPELL_BUILD_KEYBOARD_HANDLER.getCatalystBagKey().getLocalizedText().asOrderedText(),
                        x / 0.5f, y / 0.5f, 0xFFFFFF, 0x0,
                        matrix.peek().getPositionMatrix(), provider, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                provider.draw();
            }

            var drawnAspects = 0;
            for (var ignored : elements) {
                var x = width / 2 - elements.size() * 18 / 2 + drawnAspects * 18 + 6;
                var y = guiStartHeight + ELEMENT_SLOT_SIZE + 17;

                var provider = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
                client.textRenderer.drawWithOutline(Text.literal(Integer.toString(drawnAspects + 1)).asOrderedText(),
                        x / 0.5f, y / 0.5f, 0xFFFFFF, 0x0,
                        matrix.peek().getPositionMatrix(), provider, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                provider.draw();
                drawnAspects++;
            }
            matrix.pop();
        }

    }

    public void renderChannelBar(ActiveCastComponent comp, DrawContext ctx, float tickDelta) {
        var client = MinecraftClient.getInstance();
        var matrix = ctx.getMatrices();

        var age = comp.getChannelAge();
        var maxAge = comp.getChannelMaxAge();
        float factor = (maxAge - age - tickDelta) / maxAge;

        var width = client.getWindow().getScaledWidth();
        var height = client.getWindow().getScaledHeight();
        var guiStartHeight = height - 29;

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        ctx.drawTexture(CHANNEL_BAR_TEXTURE, width / 2 - 91, guiStartHeight, 0, 0,
                182, 5, 182, 10);
        ctx.drawTexture(CHANNEL_BAR_TEXTURE, width / 2 - 91, guiStartHeight, 0, 5,
                (int) (182 * factor), 5, 182, 10);
    }

    @Override
    public void onHudRender(DrawContext ctx, float tickDelta) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        var comp = client.player.getComponent(ModComponents.CAST);

        if (comp.isBuilding()) {
            this.renderBuilder(comp, ctx, tickDelta);
        }

        if (comp.isChanneling()) {
            this.renderChannelBar(comp, ctx, tickDelta);
        }
    }

    public void drawElement(DrawContext ctx, int x, int y, @Nullable SpellElement element, float scale) {
        // FIXME: possibly redundant since 1.20
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        ctx.drawTexture(FRAME_TEXTURE, x, y, 0, 0,
                (int) (ELEMENT_SLOT_SIZE * scale), (int) (ELEMENT_SLOT_SIZE * scale),
                (int) (ELEMENT_SLOT_SIZE * scale), (int) (ELEMENT_SLOT_SIZE * scale));
        var frameOffset = (ELEMENT_SLOT_SIZE - 16) / 2;

        if (element == null) {
            return;
        }

        if (element instanceof Aspect asp) {
            ctx.drawTexture(asp.getTexturePath(), x + (int) (frameOffset * scale), y + (int) (frameOffset * scale), 0, 0,
                    (int) (16 * scale), (int) (16 * scale), (int) (16 * scale), (int) (16 * scale));
            return;
        }

        if (scale < 1f) {
            frameOffset = 0; // TODO: Figure out how to scale items.
        }

        if (element instanceof ItemElement.Stack ies) {
            ctx.drawItem(ies.getStack(), x + (int) (frameOffset * scale), y + (int) (frameOffset * scale), 42);
        } else if (element instanceof ItemElement ie) {
            ctx.drawItem(ie.getDefaultStack(), x + (int) (frameOffset * scale), y + (int) (frameOffset * scale), 42);
        }
    }
}
