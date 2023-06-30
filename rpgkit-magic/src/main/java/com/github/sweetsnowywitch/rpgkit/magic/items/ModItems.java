package com.github.sweetsnowywitch.rpgkit.magic.items;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
    //public static final Item ITEM_CASTER = new CasterItem(new FabricItemSettings());
    public static final Item SPELL_ITEM = new SpellItem(new FabricItemSettings().maxCount(1));
    public static final Item CATALYST_BAG = new CatalystBagItem(new FabricItemSettings().maxCount(1));

    public static void register() {
        // Registry.register(Registries.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "caster"), ITEM_CASTER);
        Registry.register(Registry.ITEM, new Identifier(RPGKitMagicMod.MOD_ID, "spell"), SPELL_ITEM);
        Registry.register(Registry.ITEM, Identifier.of(RPGKitMagicMod.MOD_ID, "catalyst_bag"), CATALYST_BAG);
    }
}
