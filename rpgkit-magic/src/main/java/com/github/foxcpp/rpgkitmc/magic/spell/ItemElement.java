package com.github.foxcpp.rpgkitmc.magic.spell;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Objects;

public sealed class ItemElement implements SpellElement {
    public static final class Stack extends ItemElement {
        private final ItemStack stack;
        private final Inventory sourceInventory;

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
        var itemId = Registries.ITEM.getId(this.item);
        if (itemId.equals(Registries.ITEM.getDefaultId())) {
            return 0;
        }

        var costs = MagicRegistries.ITEM_COSTS.get(itemId);
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
        return Registries.ITEM.getId(this.item).toString();
    }

    public static ItemElement fromNbt(NbtCompound comp) {
        var id = Identifier.tryParse(comp.getString("Id"));
        if (id == null) {
            throw new IllegalStateException("Malformed item ID in NBT: %s".formatted(comp.getString("Id")));
        }
        return new ItemElement(Registries.ITEM.get(id));
    }

    @Override
    public void writeToNbt(NbtCompound comp) {
        comp.putString("Type", "Item");
        comp.putString("Id", Registries.ITEM.getId(this.item).toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemElement that = (ItemElement) o;
        return item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
