package com.github.sweetsnowywitch.rpgkit.magic.form;

import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellForm;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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
        if (targetStack == null || targetStack.isEmpty()) {
            ModForms.SELF.startCast(cast, world, caster);
            return;
        }

        super.startCast(cast, world, caster);

        var res = cast.getSpell().useOnItem(cast, world, targetStack, null, caster);
        caster.equipStack(EquipmentSlot.OFFHAND, res.getValue());
    }

    @Override
    public void endCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world) {
        super.endCast(cast, world);
    }
}
