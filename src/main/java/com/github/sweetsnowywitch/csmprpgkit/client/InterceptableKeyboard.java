package com.github.sweetsnowywitch.csmprpgkit.client;

import java.util.function.Function;

public interface InterceptableKeyboard {
    void intercept(int key, Function<Integer, Boolean> isActive);
    int popPressed();
}
