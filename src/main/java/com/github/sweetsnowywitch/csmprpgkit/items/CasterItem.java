package com.github.sweetsnowywitch.csmprpgkit.items;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellRayEntity;
import com.github.sweetsnowywitch.csmprpgkit.magic.Spell;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellBuilder;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import com.github.sweetsnowywitch.csmprpgkit.magic.form.ModForms;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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

        var spellBuilder = new SpellBuilder(ModForms.RAY);
        spellBuilder.addElement(SpellElement.of(ModRegistries.ASPECTS.get(Identifier.of(RPGKitMod.MOD_ID, "aer"))));
        spellBuilder.addElement(SpellElement.of(ModRegistries.ASPECTS.get(Identifier.of(RPGKitMod.MOD_ID, "interitio"))));
        spellBuilder.complete();

        var cast = spellBuilder.toCast(user);
        cast.perform();

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
