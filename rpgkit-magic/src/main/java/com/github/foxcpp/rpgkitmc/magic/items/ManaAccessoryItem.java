package com.github.foxcpp.rpgkitmc.magic.items;

import com.github.foxcpp.rpgkitmc.magic.ModAttributes;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellElement;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ManaAccessoryItem extends TrinketItem implements MagicChargeableItem {
    private final ImmutableMap<String, @NotNull Float> baseCharge;
    private final @Nullable EntityAttributeModifier manaMod;
    private final @Nullable EntityAttributeModifier manaRegenMod;
    private final @Nullable EntityAttributeModifier manaRegenSpeedMod;

    public ManaAccessoryItem(Settings settings, ImmutableMap<String, Float> baseCharge,
                             float mana, float manaRegen, float manaRegenSpeed) {
        super(settings);
        this.baseCharge = baseCharge;
        if (mana != 0) {
            this.manaMod = new EntityAttributeModifier("mana_mod", mana, EntityAttributeModifier.Operation.ADDITION);
        } else {
            this.manaMod = null;
        }
        if (manaRegen != 0) {
            this.manaRegenMod = new EntityAttributeModifier("mana_regen", manaRegen, EntityAttributeModifier.Operation.ADDITION);
        } else {
            this.manaRegenMod = null;
        }
        if (manaRegenSpeed != 0) {
            this.manaRegenSpeedMod = new EntityAttributeModifier("mana_regen_speed", manaRegenSpeed, EntityAttributeModifier.Operation.ADDITION);
        } else {
            this.manaRegenSpeedMod = null;
        }
    }

    @Nullable
    private EntityAttributeModifier getAttributeBuff(ItemStack stack, String key, @Nullable EntityAttributeModifier defaultValue) {
        var nbt = stack.getNbt();
        if (nbt != null && nbt.contains("ManaBonuses")) {
            var modifiers = nbt.getCompound("ManaBonuses");
            if (modifiers.contains(key)) {
                return new EntityAttributeModifier("nbt_" + key, modifiers.getFloat(key), EntityAttributeModifier.Operation.ADDITION);
            }
        }
        return defaultValue;
    }

    public float getMagicCharge(ItemStack stack, String key) {
        float baseValue = Objects.requireNonNull(this.baseCharge.getOrDefault(key, (float) 0));

        var values = stack.getOrCreateSubNbt("MagicCharge");
        if (values.contains(key, NbtElement.FLOAT_TYPE)) {
            return baseValue + values.getFloat(key);
        }

        return baseValue;
    }

    public void addMagicCharge(ItemStack stack, String key, float add) {
        if (add == 0) {
            return;
        }

        var current = this.getMagicCharge(stack, key);

        var nbt = stack.getOrCreateSubNbt("MagicCharge");
        nbt.putFloat(key, current + add);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        var attrs = super.getModifiers(stack, slot, entity, uuid);

        if (this.getMagicCharge(stack, SpellElement.COST_MAGICAE) <= 0) {
            return attrs;
        }

        var mana = this.getAttributeBuff(stack, "Mana", this.manaMod);
        if (mana != null) {
            attrs.put(ModAttributes.MAX_MANA, mana);
        }

        if (entity.getAttributeValue(ModAttributes.MANA_REGEN) == 0) {
            var regen = this.getAttributeBuff(stack, "Regen", this.manaRegenMod);
            if (regen != null) {
                attrs.put(ModAttributes.MANA_REGEN, regen);
            }
        }

        var regenSpeed = this.getAttributeBuff(stack, "RegenSpeed", this.manaRegenSpeedMod);
        if (regenSpeed != null) {
            attrs.put(ModAttributes.MANA_REGEN_SPEED, regenSpeed);
        }

        return attrs;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("rpgkit.magic.tooltip.amulet.mana_boost"));
        if (this.getMagicCharge(stack, SpellElement.COST_MAGICAE) > 0) {
            tooltip.add(Text.translatable("rpgkit.magic.tooltip.amulet.mana_unlock_ready1"));
            tooltip.add(Text.translatable("rpgkit.magic.tooltip.amulet.mana_unlock_ready2"));
        } else {
            tooltip.add(Text.translatable("rpgkit.magic.tooltip.amulet.mana_unlock1"));
            tooltip.add(Text.translatable("rpgkit.magic.tooltip.amulet.mana_unlock2"));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return this.getMagicCharge(stack, SpellElement.COST_MAGICAE) > 0;
    }
}