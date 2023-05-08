package com.github.sweetsnowywitch.csmprpgkit.mixin;

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
    public final Deque<InputUtil.Key> pressQueue = new ArrayDeque<>();
    public final Map<InputUtil.Key, Boolean> isPressed = new HashMap<>();
    public final Map<Integer, Function<InputUtil.Key, Boolean>> intercepted = new HashMap<>();

    @Override
    public void clear() {
        this.intercepted.clear();
        this.isPressed.clear();
        this.pressQueue.clear();
    }

    public void intercept(InputUtil.Key key, Function<InputUtil.Key, Boolean> isActive) {
        this.intercepted.put(key.getCode(), isActive);
    }

    public InputUtil.Key popPressed() {
        var i = this.pressQueue.poll();
        if (i == null) return InputUtil.UNKNOWN_KEY;
        return i;
    }

    @Inject(method = "onKey", cancellable = true,
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0))
    private void onKeyboardInput(long windowPointer, int keyCode, int scanCode, int action, int modifiers, CallbackInfo ci) {
        var key = InputUtil.fromKeyCode(keyCode, scanCode);

        var isActive = this.intercepted.get(key.getCode());
        if (isActive != null && isActive.apply(key)) {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key.getCode())) {
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
