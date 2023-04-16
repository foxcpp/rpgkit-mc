package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

public class ItemElement implements SpellElement {
    public static class Stack extends ItemElement {
        protected final ItemStack stack;
        protected final Inventory sourceInventory;

        public Stack(ItemStack stack) {
            this(stack, null);
        }

        public Stack(ItemStack stack, Inventory source) {
            super(stack.getItem());
            this.stack = stack;
            this.sourceInventory = source;
        }

        public boolean isValid(LivingEntity user) {
            if (user instanceof PlayerEntity pe && this.sourceInventory != null) {
                if (!this.sourceInventory.canPlayerUse(pe)) {
                    return false;
                }
            }

            return this.item.equals(this.stack.getItem()) && this.stack.getCount() > 0;
        }

        public ItemStack getStack() {
            return stack;
        }

        @Override
        public void consume() {
            stack.decrement(1);
            if (this.sourceInventory != null) {
                this.sourceInventory.markDirty();
            }
        }
    }

    protected final Item item;

    public ItemElement(Item stack) {
        this.item = stack;
    }

    @Override
    public float getBaseCost(String key) {
        var itemId = Registry.ITEM.getId(this.item);
        if (itemId.equals(Registry.ITEM.getDefaultId())) {
            return 0;
        }

        var costs = ModRegistries.ITEM_COSTS.get(itemId);
        if (costs == null) {
            return 0;
        }
        return costs.getOrDefault(key, 0f);
    }

    public int getColor() {
        return 0xFFFFFFFF;
    }

    public Item getItem() {
        return this.item;
    }

    public String toString() {
        return Registry.ITEM.getId(this.item).toString();
    }
}
