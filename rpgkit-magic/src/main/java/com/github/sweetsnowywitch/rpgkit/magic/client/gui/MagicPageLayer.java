package com.github.sweetsnowywitch.rpgkit.magic.client.gui;

import com.github.clevernucleus.playerex.api.EntityAttributeSupplier;
import com.github.clevernucleus.playerex.api.client.ClientUtil;
import com.github.clevernucleus.playerex.api.client.RenderComponent;
import com.github.sweetsnowywitch.rpgkit.client.gui.RPGKitPageLayer;
import com.github.sweetsnowywitch.rpgkit.magic.ModAttributes;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.components.ModComponents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class MagicPageLayer extends RPGKitPageLayer {
    public static final EntityAttributeSupplier MANA_REGEN_SPEED = EntityAttributeSupplier.of(new Identifier(RPGKitMagicMod.MOD_ID, "mana_regen_speed"));

    public MagicPageLayer(HandledScreen<?> parent, ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(parent, handler, inventory, title);
    }

    static {
        COMPONENTS.add(RenderComponent.of((entity) -> {
            var current = ClientUtil.FORMATTING_2.format(entity.getComponent(ModComponents.MANA).getValue());
            var maximum = ClientUtil.FORMATTING_2.format(entity.getAttributeValue(ModAttributes.MAX_MANA));
            return Text.translatable("rpgkit.gui.page.attributes.text.max_mana", current, maximum).formatted(Formatting.DARK_GRAY);
        }, (entity) -> {
            var tooltip = new ArrayList<Text>();
            tooltip.add(Text.translatable("rpgkit.gui.page.attributes.tooltip.max_mana").formatted(Formatting.GRAY));
            return tooltip;
        }, 9, 80));
        COMPONENTS.add(RenderComponent.of(MANA_REGEN_SPEED, (value) -> Text.translatable("rpgkit.gui.page.attributes.text.mana_regen_speed",
                        ClientUtil.FORMATTING_2.format(value)).formatted(Formatting.DARK_GRAY),
                (value) -> {
                    var tooltip = new ArrayList<Text>();
                    tooltip.add(Text.translatable("rpgkit.gui.page.attributes.tooltip.mana_regen_speed")
                            .formatted(Formatting.GRAY));
                    return tooltip;
                }, 9, 91));
    }
}
