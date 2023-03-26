package com.github.sweetsnowywitch.csmprpgkit.classes;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import net.minecraft.util.math.random.Random;

public class Dice {
    private static final Random random = Random.create();
    public static int roll(int max, int count, int mod){
        int result = 0;
        for (int x = 0; x < count; x++) {
            result += random.nextBetween(1, max);
            RPGKitMod.LOGGER.debug("rolled d{}: {}", max, result);
        }
        result += mod;
        return result;
    }
}
