package com.github.foxcpp.rpgkitmc.magic.effects;

import com.github.foxcpp.rpgkitmc.magic.effects.item.ItemEffects;
import com.github.foxcpp.rpgkitmc.magic.effects.use.UseEffects;
import com.github.foxcpp.rpgkitmc.magic.effects.area.AreaEffects;

public class ModEffects {
    public static void register() {
        UseEffects.register();
        AreaEffects.register();
        ItemEffects.register();
    }
}
