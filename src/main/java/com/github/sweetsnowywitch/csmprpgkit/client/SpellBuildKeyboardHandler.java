package com.github.sweetsnowywitch.csmprpgkit.client;

import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SpellBuildKeyboardHandler implements ClientTickEvents.EndTick {
    private @Nullable InterceptableKeyboard keyboard = null;

    private boolean isActive(MinecraftClient client) {
        return client.player != null && client.player.getComponent(ModComponents.CAST).isBuilding();
    }

    private void setupKeyboard(MinecraftClient client) {
        this.keyboard = ((InterceptableKeyboard)client.keyboard);
        for (int key = GLFW.GLFW_KEY_1; key <= GLFW.GLFW_KEY_9; key++) {
            this.keyboard.intercept(key, (k) -> this.isActive(client));
        }
        this.keyboard.intercept(GLFW.GLFW_KEY_Q, (k) -> this.isActive(client));
        this.keyboard.intercept(GLFW.GLFW_KEY_F, (k) -> this.isActive(client));
        this.keyboard.intercept(GLFW.GLFW_KEY_TAB, (k) -> this.isActive(client));
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

        int key;
        while ((key = this.keyboard.popPressed()) != 0) {
            for (int aspKey = GLFW.GLFW_KEY_1; aspKey <= GLFW.GLFW_KEY_9; aspKey++) {
                if (aspKey == key && (aspKey - GLFW.GLFW_KEY_1) < comp.getAvailableElements().size()) {
                    comp.addElement(aspKey - GLFW.GLFW_KEY_1);
                }
            }

            if (key == GLFW.GLFW_KEY_TAB) {
                comp.switchCatalystBag();
            }
            if (key == GLFW.GLFW_KEY_Q) {
                comp.performAreaCast();
            }
        }
    }
}
