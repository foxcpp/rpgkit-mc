package com.github.sweetsnowywitch.csmprpgkit.client;

import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SpellBuildKeyboardHandler implements ClientTickEvents.EndTick {
    private InputUtil.Key areaCastKey = InputUtil.fromKeyCode(GLFW.GLFW_KEY_Q, -1);
    private InputUtil.Key itemCastKey = InputUtil.fromKeyCode(GLFW.GLFW_KEY_F, -1);
    private InputUtil.Key catalystBagKey = InputUtil.fromKeyCode(GLFW.GLFW_KEY_TAB, -1);

    private @Nullable InterceptableKeyboard keyboard = null;

    private boolean isActive(MinecraftClient client) {
        return client.player != null && client.player.getComponent(ModComponents.CAST).isBuilding();
    }

    public InputUtil.Key getAreaCastKey() {
        return areaCastKey;
    }

    public InputUtil.Key getItemCastKey() {
        return itemCastKey;
    }

    public InputUtil.Key getCatalystBagKey() {
        return catalystBagKey;
    }

    private void setupKeyboard(MinecraftClient client) {
        this.keyboard = ((InterceptableKeyboard)client.keyboard);
        this.keyboard.clear();
        for (int key = GLFW.GLFW_KEY_1; key <= GLFW.GLFW_KEY_9; key++) {
            this.keyboard.intercept(InputUtil.fromKeyCode(key, -1), (k) -> this.isActive(client));
        }
        this.keyboard.intercept(areaCastKey, (k) -> this.isActive(client));
        this.keyboard.intercept(itemCastKey, (k) -> this.isActive(client));
        this.keyboard.intercept(catalystBagKey, (k) -> this.isActive(client));
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (this.keyboard == null) {
            this.setupKeyboard(client);
        }

        if (client.player == null) {
            return;
        }
        var comp = client.player.getComponent(ModComponents.CAST);

        while (ClientRPGKitMod.ACTIVATE_SPELL_BUILD_KEY.wasPressed()) {
            if (comp.isBuilding()) {
                comp.performSelfCast();
            } else {
                comp.startBuild();
            }
        }

        if (!comp.isBuilding()) {
            return;
        }

        InputUtil.Key key;
        while ((key = this.keyboard.popPressed()) != InputUtil.UNKNOWN_KEY) {
            for (int aspKey = GLFW.GLFW_KEY_1; aspKey <= GLFW.GLFW_KEY_9; aspKey++) {
                if (aspKey == key.getCode() && (aspKey - GLFW.GLFW_KEY_1) < comp.getAvailableElements().size()) {
                    comp.addElement(aspKey - GLFW.GLFW_KEY_1);
                }
            }

            if (key.getCode() == catalystBagKey.getCode()) {
                comp.switchCatalystBag();
            }
            if (key.getCode() == areaCastKey.getCode()) {
                comp.performAreaCast();
            }
        }
    }
}
