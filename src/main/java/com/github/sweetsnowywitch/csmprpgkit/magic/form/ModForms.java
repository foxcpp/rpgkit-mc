package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModForms {
    public static final SpellForm SELF = new SelfForm();
    public static final SpellForm RAY = new RayForm();
    public static final SpellForm AREA = new AreaForm();
    public static final SpellForm BLAST = new BlastForm();
    public static final SpellForm CHARGE = new ChargeForm();
    public static final SpellForm HITSCAN = new HitscanForm();
    public static void register() {
        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "self"), SELF);

        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "ray"), RAY);

        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "area"), AREA);
        Registry.register(ModRegistries.SPELL_FORM_REACTIONS, Identifier.of(RPGKitMod.MOD_ID, "area"),
                SpellReaction.factoryFor(AreaForm.Reaction::new, AreaForm.Reaction::new));

        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "blast"), BLAST);
        Registry.register(ModRegistries.SPELL_FORM_REACTIONS, Identifier.of(RPGKitMod.MOD_ID, "blast"),
                SpellReaction.factoryFor(BlastForm.Reaction::new, BlastForm.Reaction::new));

        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "charge"), CHARGE);
        Registry.register(ModRegistries.SPELL_FORM_REACTIONS, Identifier.of(RPGKitMod.MOD_ID, "charge"),
                SpellReaction.factoryFor(ChargeForm.Reaction::new, ChargeForm.Reaction::new));

        Registry.register(ModRegistries.SPELL_FORMS, Identifier.of(RPGKitMod.MOD_ID, "hitscan"), HITSCAN);
    }
}
