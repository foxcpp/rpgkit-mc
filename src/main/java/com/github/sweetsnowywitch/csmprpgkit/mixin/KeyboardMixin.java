package com.github.sweetsnowywitch.csmprpgkit.mixin;

import com.github.sweetsnowywitch.csmprpgkit.client.ClientRPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.client.InterceptableKeyboard;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Mixin(Keyboard.class)
public class KeyboardMixin implements InterceptableKeyboard {
    public final Deque<Integer> pressQueue = new ArrayDeque<>();
    public final Map<Integer, Boolean> isPressed = new HashMap<>();
    public final Map<Integer, Function<Integer, Boolean>> intercepted = new HashMap<>();

    public void intercept(int key, Function<Integer, Boolean> isActive) {
        this.intercepted.put(key, isActive);
    }

    public int popPressed() {
        var i = this.pressQueue.poll();
        if (i == null) return 0;
        return i;
    }

    @Inject(method = "onKey", cancellable = true,
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0))
    private void onKeyboardInput(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci)
    {
        var isActive = this.intercepted.get(key);
        if (isActive != null && isActive.apply(key)) {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key)) {
                if (!isPressed.getOrDefault(key, false)) {
                    pressQueue.add(key);
                    isPressed.put(key, true);
                }
            } else {
                isPressed.put(key, false);
            }
            ci.cancel();
        } else {
            isPressed.clear();
        }
    }
}
