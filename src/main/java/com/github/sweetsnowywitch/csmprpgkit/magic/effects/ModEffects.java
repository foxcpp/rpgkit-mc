package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.magic.effects.area.AreaEffects;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.item.ItemEffects;
import com.github.sweetsnowywitch.csmprpgkit.magic.effects.use.UseEffects;

public class ModEffects {
    public static void register() {
        UseEffects.register();
        AreaEffects.register();
        ItemEffects.register();
    }
}
