package com.github.sweetsnowywitch.rpgkit.magic.effects;

import com.github.sweetsnowywitch.rpgkit.magic.effects.area.AreaEffects;
import com.github.sweetsnowywitch.rpgkit.magic.effects.item.ItemEffects;
import com.github.sweetsnowywitch.rpgkit.magic.effects.use.UseEffects;

public class ModEffects {
    public static void register() {
        UseEffects.register();
        AreaEffects.register();
        ItemEffects.register();
    }
}
