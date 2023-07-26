package com.github.foxcpp.rpgkitmc.client.gui;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class ClassPageLayer extends RPGKitPageLayer{
    public ClassPageLayer(HandledScreen<?> parent, ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(parent, handler, inventory, title);
    }
}
