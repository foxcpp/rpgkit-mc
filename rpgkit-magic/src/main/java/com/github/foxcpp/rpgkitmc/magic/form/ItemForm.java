package com.github.foxcpp.rpgkitmc.magic.form;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellElement;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellForm;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ItemForm extends SpellForm {
    public ItemForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(@NotNull ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        ItemStack targetStack = null;
        if (caster instanceof LivingEntity le) {
            targetStack = le.getOffHandStack();
        } else {
            var items = caster.getHandItems();
            if (items.iterator().hasNext()) {
                targetStack = items.iterator().next();
            }
        }
        if (targetStack == null) {
            targetStack = ItemStack.EMPTY.copy();
        }

        if (targetStack.getCount() > 1) {
            var cost = cast.getCost(SpellElement.COST_MAGICAE);
            var ms = cast.getManaSource(world);
            if (ms != null) {
                if (!ms.spendMana(cost * (targetStack.getCount() - 1))) {
                    RPGKitMagicMod.LOGGER.info("Cast failed due to mana overspending (too many items) ({}) of {}", cost, ms);
                    var player = cast.getPlayerCaster(world);
                    if (player != null) {
                        player.sendMessage(Text.translatable("rpgkit.magic.not_enough_mana"), true);
                    }
                    return;
                }
            } else {
                return;
            }
        }

        super.startCast(cast, world, caster);

        var res = cast.getSpell().useOnItem(cast, world, targetStack, /* TODO */ null, caster);
        caster.equipStack(EquipmentSlot.OFFHAND, res.getValue());
    }

    @Override
    public void endCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world) {
        super.endCast(cast, world);
    }
}
