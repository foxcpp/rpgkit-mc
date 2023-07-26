package com.github.foxcpp.rpgkitmc.magic.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MagicFuelItem extends Item {
    public MagicFuelItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null) {
            target.setOnFire(true);
            target.setOnFireFor(5);
        }
        if (attacker != null) {
            attacker.setOnFire(true);
            attacker.setOnFireFor(1);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (selected && entity != null) {
            entity.setOnFire(true);
            entity.setOnFireFor(5);
            stack.decrement(1);
        }
    }
}
