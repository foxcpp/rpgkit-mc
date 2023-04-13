package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModForms {
    public static final SpellForm SELF = new SelfForm();
    public static final SpellForm RAY = new RayForm();

    public static final SpellForm AREA = new AreaForm();
    public static void register() {
        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "self"), SELF);
        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "ray"), RAY);
        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "area"), AREA);
    }
}
