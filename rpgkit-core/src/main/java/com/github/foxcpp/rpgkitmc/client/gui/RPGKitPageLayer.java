package com.github.foxcpp.rpgkitmc.client.gui;

import com.github.clevernucleus.playerex.PlayerEx;
import com.github.clevernucleus.playerex.api.ExAPI;
import com.github.clevernucleus.playerex.api.PlayerData;
import com.github.clevernucleus.playerex.api.client.ClientUtil;
import com.github.clevernucleus.playerex.api.client.PageLayer;
import com.github.clevernucleus.playerex.api.client.RenderComponent;
import com.github.clevernucleus.playerex.client.PlayerExClient;
import com.github.clevernucleus.playerex.client.gui.widget.ScreenButtonWidget;
import com.github.foxcpp.rpgkitmc.RPGKitMod;
import com.github.foxcpp.rpgkitmc.classes.ServerButtonHandler;
import com.github.foxcpp.rpgkitmc.components.ModComponents;
import com.github.foxcpp.rpgkitmc.components.entity.ClassComponent;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class RPGKitPageLayer extends PageLayer {
    private static final Supplier<Float> scaleX = () -> ExAPI.getConfig().textScaleX();
    private static final Supplier<Float> scaleY = () -> ExAPI.getConfig().textScaleY();
    private static final float scaleZ = 0.75F;
    private static final List<Identifier> BUTTON_KEYS;
    private PlayerData playerData;
    private final Map<Identifier, Integer> buttonDelay = new HashMap<>();
    public static final Identifier PACKET_ID = ServerButtonHandler.PACKET_ID;
    public static final List<RenderComponent> COMPONENTS = new ArrayList<>();

    public RPGKitPageLayer(HandledScreen<?> parent, ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(parent, handler, inventory, title);
        this.buttonDelay.put(new Identifier(RPGKitMod.MOD_ID, "class_points"), 0);
        this.buttonDelay.put(new Identifier(RPGKitMod.MOD_ID, "rouge"), 1);
        this.buttonDelay.put(new Identifier(RPGKitMod.MOD_ID, "warrior"), 2);
        this.buttonDelay.put(new Identifier(RPGKitMod.MOD_ID, "wizard"), 3);
    }

    private boolean canRefund() {
        return this.playerData.refundPoints() > 0;
    }

    private void forEachScreenButton(Consumer<ScreenButtonWidget> consumer) {
        this.children().stream().filter((e) -> e instanceof ScreenButtonWidget).forEach((e) -> {
            consumer.accept((ScreenButtonWidget) e);
        });
    }

    public void buttonPressed(ButtonWidget buttonIn) {
        var button = (ScreenButtonWidget) buttonIn;
        var key = button.key();
        var player = this.client.player;
        var buf = PacketByteBufs.create();
        if (key.equals(new Identifier(RPGKitMod.MOD_ID, "class_points"))
                && (this.playerData.get(ExAPI.LEVEL) - player.getComponent(ModComponents.CLASS).getTotalExp()) > 0) {
            this.remove(new ScreenButtonWidget(this.parent, 8, 53, 204, 0, 11, 10, BUTTON_KEYS.get(0), this::buttonPressed, this::buttonTooltip));
            buf.writeString(ServerButtonHandler.Action.PRESS_CP_BUTTON.name());
            buf.writeInt(1);
            ClientPlayNetworking.send(PACKET_ID, buf);
            this.addDrawableChild(new ScreenButtonWidget(this.parent, 8, 53, 204, 0, 11, 10, BUTTON_KEYS.get(0), this::buttonPressed, this::buttonTooltip));
        } else {
            buf.writeString(ServerButtonHandler.Action.PRESS_CLASS_BUTTON.name());
            buf.writeIdentifier(key);
            ClientPlayNetworking.send(PACKET_ID, buf);
        }
        player.playSound(PlayerEx.SP_SPEND_SOUND, SoundCategory.NEUTRAL, ExAPI.getConfig().skillUpVolume(), 1.5F);
        this.buttonDelay.put(key, 40);
    }

    public void buttonTooltip(ButtonWidget buttonIn, MatrixStack matrices, int mouseX, int mouseY) {
        var button = (ScreenButtonWidget) buttonIn;
        var key = button.key();
        var text = "rpgkit.gui.page.attributes.tooltip.button.class_" + key.getPath();
        var tooltip = Text.translatable(text).formatted(Formatting.GRAY);
        if (key.equals(new Identifier(RPGKitMod.MOD_ID, "class_points"))) {
            var player = this.client.player;
            var requiredXp = ClassComponent.REQUIRED_LEVEL_EXP[Math.min(player.getComponent(ModComponents.CLASS).getTotalExp() + 1, ClassComponent.REQUIRED_LEVEL_EXP.length - 1)];
            var currentXp = player.getComponent(ModComponents.CLASS).getCurrentLevelExp();
            var progress = "(" + currentXp + "/" + requiredXp + ")";
            tooltip = Text.translatable(text, progress).formatted(Formatting.GRAY);
        }
        this.renderTooltip(matrices, tooltip, mouseX, mouseY);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        matrices.push();
        matrices.scale(scaleX.get(), scaleY.get(), scaleZ);
        COMPONENTS.forEach((component) -> {
            component.renderText(this.client.player, matrices, this.textRenderer, this.x, this.y, scaleX.get(), scaleY.get());
        });
        matrices.pop();
        COMPONENTS.forEach((component) -> {
            component.renderTooltip(this.client.player, this::renderTooltip, matrices, this.textRenderer, this.x, this.y, mouseX, mouseY, scaleX.get(), scaleY.get());
        });
    }

    public void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, PlayerExClient.GUI);
        this.forEachScreenButton((button) -> {
            var key = button.key();
            var player = this.client.player;
            if (BUTTON_KEYS.contains(key)) {
                if (key.equals(new Identifier(RPGKitMod.MOD_ID, "class_points"))) {
                    button.active = (this.playerData.get(ExAPI.LEVEL) - player.getComponent(ModComponents.CLASS).getTotalExp()) > 0;
                } else {
                    button.active = player.getComponent(ModComponents.CLASS).getUndistributedLevels() > 0;
                }
                button.alt = this.canRefund();
                var buttonDelay = this.buttonDelay.getOrDefault(key, 0);
                button.active &= buttonDelay == 0;
                if (buttonDelay > 0) {
                    this.buttonDelay.put(key, Math.max(0, buttonDelay - 1));
                }
            }
        });
    }

    protected void init() {
        super.init();
        assert this.client != null;
        this.playerData = ExAPI.PLAYER_DATA.get(Objects.requireNonNull(this.client.player));
        this.addDrawableChild(new ScreenButtonWidget(this.parent, 8, 23, 204, 0, 11, 10, BUTTON_KEYS.get(0), this::buttonPressed, this::buttonTooltip));
        this.addDrawableChild(new ScreenButtonWidget(this.parent, 8, 37, 204, 0, 11, 10, BUTTON_KEYS.get(1), this::buttonPressed, this::buttonTooltip));
        this.addDrawableChild(new ScreenButtonWidget(this.parent, 8, 48, 204, 0, 11, 10, BUTTON_KEYS.get(2), this::buttonPressed, this::buttonTooltip));
        this.addDrawableChild(new ScreenButtonWidget(this.parent, 8, 59, 204, 0, 11, 10, BUTTON_KEYS.get(3), this::buttonPressed, this::buttonTooltip));
    }

    static {
        BUTTON_KEYS = ImmutableList.of(new Identifier(RPGKitMod.MOD_ID, "class_points"), new Identifier(RPGKitMod.MOD_ID, "rouge"), new Identifier(RPGKitMod.MOD_ID, "warrior"), new Identifier(RPGKitMod.MOD_ID, "wizard"));
        COMPONENTS.add(RenderComponent.of((entity) -> {
            var current = ClientUtil.FORMATTING_2.format(entity.getComponent(ModComponents.CLASS).getUndistributedLevels());
            return Text.translatable("rpgkit.gui.page.attributes.text.class_points", current).formatted(Formatting.DARK_GRAY);
        }, (entity) -> {
            var tooltip = new ArrayList<Text>();
            tooltip.add(Text.translatable("rpgkit.gui.page.attributes.tooltip.class_points[0]").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("rpgkit.gui.page.attributes.tooltip.class_points[1]").formatted(Formatting.GRAY));
            return tooltip;
        }, 21, 26));
        COMPONENTS.add(RenderComponent.of((entity) -> {
            var current = ClientUtil.FORMATTING_2.format(entity.getComponent(ModComponents.CLASS).getClassLevel(new Identifier(RPGKitMod.MOD_ID, "rouge")));
            return Text.translatable("rpgkit.gui.page.attributes.text.class_rouge", current).formatted(Formatting.DARK_GRAY);
        }, (entity) -> {
            var tooltip = new ArrayList<Text>();
            tooltip.add(Text.translatable("rpgkit.gui.page.attributes.tooltip.class_rouge").formatted(Formatting.GRAY));
            return tooltip;
        }, 21, 40));
        COMPONENTS.add(RenderComponent.of((entity) -> {
            var current = ClientUtil.FORMATTING_2.format(entity.getComponent(ModComponents.CLASS).getClassLevel(new Identifier(RPGKitMod.MOD_ID, "warrior")));
            return Text.translatable("rpgkit.gui.page.attributes.text.class_warrior", current).formatted(Formatting.DARK_GRAY);
        }, (entity) -> {
            var tooltip = new ArrayList<Text>();
            tooltip.add(Text.translatable("rpgkit.gui.page.attributes.tooltip.class_warrior").formatted(Formatting.GRAY));
            return tooltip;
        }, 21, 51));
    }
}
