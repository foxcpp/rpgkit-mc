package com.github.foxcpp.rpgkitmc.magic.items;

import com.github.foxcpp.rpgkitmc.magic.spell.SpellElement;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface MagicChargeableItem {
    float getMagicCharge(ItemStack stack, String key);

    void addMagicCharge(ItemStack stack, String key, float add);

    static ItemStack getChargedStack(Item item, float magicae) {
        var st = new ItemStack(item);
        if (item instanceof MagicChargeableItem mci) {
            mci.addMagicCharge(st, SpellElement.COST_MAGICAE, magicae);
        }
        return st;
    }
}
