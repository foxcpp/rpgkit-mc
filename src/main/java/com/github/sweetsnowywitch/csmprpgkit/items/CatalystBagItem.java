package com.github.sweetsnowywitch.csmprpgkit.items;

import com.github.sweetsnowywitch.csmprpgkit.screen.CatalystBagScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CatalystBagItem extends Item {
    public static final class BagInventory extends SimpleInventory {
        private final ItemStack stack;

        BagInventory(@NotNull ItemStack stack) {
            super(6);

            if (!stack.getItem().equals(ModItems.CATALYST_BAG)) {
                throw new IllegalStateException("Attempting to create BagInventory from non-bag item");
            }

            this.stack = stack;
            var nbt = stack.getNbt();
            if (nbt != null) {
                Inventories.readNbt(nbt, this.stacks);
            }
        }

        @Override
        public void markDirty() {
            super.markDirty();
            if (!this.stack.getItem().equals(ModItems.CATALYST_BAG)) return;
            Inventories.writeNbt(stack.getOrCreateNbt(), this.stacks);
        }

        @Override
        public boolean canPlayerUse(PlayerEntity player) {
            return player.getInventory().contains(this.stack) && this.stack.getItem().equals(ModItems.CATALYST_BAG);
        }

        @Override
        public void onOpen(PlayerEntity player) {
            super.onOpen(player);
            CatalystBagItem.setOpen(stack, true);
            player.getInventory().markDirty();
        }

        @Override
        public void onClose(PlayerEntity player) {
            super.onClose(player);
            CatalystBagItem.setOpen(stack, false);
            player.getInventory().markDirty();
        }
    }

    public CatalystBagItem(Settings settings) {
        super(settings);
    }

    public static Inventory getInventory(ItemStack stack) {
        return new BagInventory(stack);
    }

    public static boolean isOpen(ItemStack stack) {
        if (stack.hasNbt()) {
            assert stack.getNbt() != null;
            return stack.getNbt().getBoolean("Open");
        }
        return false;
    }

    public static void setOpen(ItemStack stack, boolean val) {
        stack.getOrCreateNbt().putBoolean("Open", val);
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
}
