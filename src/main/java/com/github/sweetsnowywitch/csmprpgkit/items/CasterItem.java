package com.github.sweetsnowywitch.csmprpgkit.items;

import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CasterItem extends Item {
    private final int price;

    public CasterItem(int price, Settings settings) {
        super(settings);
        this.price = price;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient)
            return super.use(world, user, hand);
        user.getComponent(ModComponents.MANA).spendMana(price);
        return super.use(world, user, hand);
    }
}
