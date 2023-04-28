package com.github.sweetsnowywitch.csmprpgkit.screen;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.items.CatalystBagItem;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import net.fabricmc.fabric.impl.biome.modification.BuiltInRegistryKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class CatalystBagScreenHandler extends ScreenHandler {
    private final ScreenHandlerType<CatalystBagScreenHandler> type;
    private final Inventory inventory;
    public CatalystBagScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(6));
    }

    public CatalystBagScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        this(RPGKitMod.CATALYST_BAG_SCREEN_HANDLER, syncId, playerInventory, inventory);
    }

    protected CatalystBagScreenHandler(ScreenHandlerType<CatalystBagScreenHandler> type, int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(RPGKitMod.CATALYST_BAG_SCREEN_HANDLER, syncId);
        this.type = type;

        int j;
        int i;
        ScreenHandler.checkSize(inventory, 6);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);
        for (j = 0; j < 6; ++j) {
            this.addSlot(new Slot(inventory, j, 35 + j * 18, 20));
        }
        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, j * 18 + 51));
            }
        }
        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 109));
        }
    }

    @Override
    public ScreenHandlerType<?> getType() {
        return type;
    }

    @Override
    public void onSlotClick(int slotId, int clickData, SlotActionType actionType, PlayerEntity playerEntity) {
        if (slotId >= 0) { // slotId < 0 are used for networking internals
            var catalystTag = TagKey.of(Registry.ITEM_KEY, Identifier.of(RPGKitMod.MOD_ID, "catalyst"));
            ItemStack stack = this.getSlot(slotId).getStack();

            if (stack.getItem().equals(ModItems.CATALYST_BAG)) {
                return;
            }
            if (!stack.isIn(catalystTag) && !stack.isOf(Items.AIR)) {
                return;
            }

            if (actionType == SlotActionType.SWAP) {
                var playerStack = playerEntity.getInventory().getStack(clickData);

                if (playerStack.getItem().equals(ModItems.CATALYST_BAG)) {
                    return;
                }
                if (!playerStack.isIn(catalystTag) && !playerStack.isOf(Items.AIR)) {
                    return;
                }
            }
        }

        super.onSlotClick(slotId, clickData, actionType, playerEntity);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < this.inventory.size() ? !this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true) : !this.insertItem(itemStack2, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return itemStack;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.inventory.onClose(player);
    }
}
