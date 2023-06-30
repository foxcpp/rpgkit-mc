package com.github.sweetsnowywitch.rpgkit.magic.form;

import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellForm;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModForms {
    public static final SpellForm SELF = new SelfForm();
    public static final SpellForm RAY = new RayForm();
    public static final SpellForm AREA = new AreaForm();
    public static final SpellForm BLAST = new BlastForm();
    public static final SpellForm CHARGE = new ChargeForm();
    public static final SpellForm HITSCAN = new HitscanForm();
    public static final SpellForm ITEM = new ItemForm();
    public static final SpellForm USE = new UseForm();

    public static void register() {
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "self"), SELF);
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "ray"), RAY);
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "area"), AREA);
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "blast"), BLAST);
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "charge"), CHARGE);
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "hitscan"), HITSCAN);
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "item"), ITEM);
        Registry.register(MagicRegistries.FORMS, Identifier.of(RPGKitMagicMod.MOD_ID, "use"), USE);

        Registry.register(MagicRegistries.REACTIONS, Identifier.of(RPGKitMagicMod.MOD_ID, "charge"), ChargeForm.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, Identifier.of(RPGKitMagicMod.MOD_ID, "blast"), BlastForm.Reaction::new);
        Registry.register(MagicRegistries.REACTIONS, Identifier.of(RPGKitMagicMod.MOD_ID, "area"), AreaForm.Reaction::new);
    }
}
