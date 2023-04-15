package com.github.sweetsnowywitch.csmprpgkit.items;

import com.github.sweetsnowywitch.csmprpgkit.screen.CatalystBagScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CatalystBagItem extends Item {
    public static final class BagInventory implements Inventory {
        private final ItemStack stack;
        private final DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);

        BagInventory(ItemStack stack) {
            this.stack = stack;
            if (stack.getNbt() != null) {
                Inventories.readNbt(stack.getNbt(), this.items);
            }
        }

        @Override
        public int size() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < this.size(); i++) {
                ItemStack stack = this.getStack(i);
                if (!stack.isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public ItemStack getStack(int slot) {
            return this.items.get(slot);
        }

        @Override
        public ItemStack removeStack(int slot, int amount) {
            ItemStack result = Inventories.splitStack(this.items, slot, amount);
            if (!result.isEmpty()) {
                this.markDirty();
            }
            return result;
        }

        @Override
        public ItemStack removeStack(int slot) {
            return Inventories.removeStack(this.items, slot);
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            this.items.set(slot, stack);

            if (stack.getCount() > getMaxCountPerStack()) {
                stack.setCount(getMaxCountPerStack());
            }
        }

        @Override
        public void markDirty() {
            Inventories.writeNbt(stack.getOrCreateNbt(), this.items);
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return player.getInventory().contains(this.stack);
        }

        @Override
        public void clear() {
            this.items.clear();
        }
    }

    public CatalystBagItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack inHand = user.getStackInHand(hand);
        user.openHandledScreen(this.createScreenHandlerFactory(inHand));
        return TypedActionResult.success(inHand);
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(ItemStack stack) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) ->
                new CatalystBagScreenHandler(syncId, inventory, new BagInventory(stack)), stack.getName());
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
