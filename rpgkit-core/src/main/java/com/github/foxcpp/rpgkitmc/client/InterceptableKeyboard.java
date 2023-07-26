package com.github.foxcpp.rpgkitmc.client;

import net.minecraft.client.util.InputUtil;

import java.util.function.Function;

public interface InterceptableKeyboard {
    void clear();

    void intercept(InputUtil.Key key, Function<InputUtil.Key, Boolean> isActive);

    InputUtil.Key popPressed();
}
