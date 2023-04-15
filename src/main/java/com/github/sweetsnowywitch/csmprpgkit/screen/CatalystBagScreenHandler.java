package com.github.sweetsnowywitch.csmprpgkit.screen;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.items.CatalystBagItem;
import net.fabricmc.fabric.impl.biome.modification.BuiltInRegistryKeys;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class CatalystBagScreenHandler extends Generic3x3ContainerScreenHandler {
    private final ScreenHandlerType<?> type;
    public CatalystBagScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public CatalystBagScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        this(RPGKitMod.CATALYST_BAG_SCREEN_HANDLER, syncId, playerInventory, inventory);
    }

    protected CatalystBagScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(syncId, playerInventory, inventory);
        this.type = type;
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

            if (!stack.isIn(catalystTag) && !stack.getItem().equals(Items.AIR)) {
                return;
            }

            if (actionType == SlotActionType.SWAP) {
                var playerStack = playerEntity.getInventory().getStack(clickData);

                if (!playerStack.isIn(catalystTag) && !stack.getItem().equals(Items.AIR)) {
                    return;
                }
            }
        }

        super.onSlotClick(slotId, clickData, actionType, playerEntity);
    }
}
