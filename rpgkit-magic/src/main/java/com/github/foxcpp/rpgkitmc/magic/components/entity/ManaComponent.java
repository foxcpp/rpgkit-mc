package com.github.foxcpp.rpgkitmc.magic.components.entity;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import com.github.foxcpp.rpgkitmc.magic.items.ManaAccessoryItem;
import com.github.foxcpp.rpgkitmc.magic.ManaSource;
import com.github.foxcpp.rpgkitmc.magic.ModAttributes;
import dev.emi.trinkets.api.TrinketsApi;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class ManaComponent implements AutoSyncedComponent, ServerTickingComponent, ManaSource {
    private final LivingEntity provider;
    private double value;

    public ManaComponent(LivingEntity provider) {
        this.provider = provider;
        this.value = 200;
    }

    @Override
    public void serverTick() {
        this.regenerate(this.getRegen(), this.getRegenSpeed(), this.getMaxValue());
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getDouble("ManaValue");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putDouble("ManaValue", this.value);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player.equals(this.provider);
    }

    public void regenerate(double regen, double speed, double maxValue) {
        if (this.value < maxValue && RPGKitMagicMod.RANDOM.nextFloat() <= speed)
            this.value += regen;
        else if (this.value > maxValue)
            this.value = maxValue;
        ModComponents.MANA.sync(this.provider);
    }

    public void addMana(double value) {
        this.value += value;
        var max = this.getMaxValue();
        if (this.value > max) {
            this.value = max;
        }
        ModComponents.MANA.sync(this.provider);
    }

    private boolean spendHealthAsMana(double cost) {
        var ok = true;

        var damage = (float) (cost * this.getHealthMultiplier());
        if (damage > this.provider.getHealth() - 0.5f) {
            damage = this.provider.getHealth() - 0.5f;
            ok = false;
        }
        if (this.provider.getHealth() <= 1) {
            damage = 10;
            ok = false;
        }
        this.provider.damage(this.provider.getDamageSources().magic(), damage);

        return ok;
    }

    public boolean spendMana(double cost) {
        if (this.provider instanceof PlayerEntity p && p.getAbilities().creativeMode) {
            return true;
        }

        var ok = true;

        this.value -= cost;
        if (this.value < 0) {
            var trinkets = TrinketsApi.getTrinketComponent(this.provider);
            if (trinkets.isPresent()) {
                var t = trinkets.get();
                for (var manaItem : t.getEquipped(stack -> stack.getItem() instanceof ManaAccessoryItem)) {
                    var durability = manaItem.getRight().getMaxDamage() - manaItem.getRight().getDamage();
                    var restoredMana = Math.min(durability, -this.value);
                    manaItem.getRight().damage((int) restoredMana, this.provider, ent ->
                            TrinketsApi.onTrinketBroken(manaItem.getRight(), manaItem.getLeft(), ent));
                    if (this.provider.damage(this.provider.getDamageSources().magic(), 1)) {
                        this.provider.getWorld().playSoundFromEntity(null, this.provider,
                                Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK), SoundCategory.PLAYERS,
                                1f, 0.4f, 1);
                    }
                    this.value += restoredMana;
                    if (this.value > 0) {
                        break;
                    }
                }
            }

            ok = this.spendHealthAsMana(-this.value);
            this.value = 0;
        }
        ModComponents.MANA.sync(this.provider);
        return ok;
    }

    public double getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        ModComponents.MANA.sync(this.provider);
    }

    public double getMaxValue() {
        var attr = this.provider.getAttributeInstance(ModAttributes.MAX_MANA);
        if (attr == null) {
            return ModAttributes.MAX_MANA.getDefaultValue();
        }
        return attr.getValue();
    }

    public double getRegen() {
        var attr = this.provider.getAttributeInstance(ModAttributes.MANA_REGEN);
        if (attr == null) {
            return ModAttributes.MANA_REGEN.getDefaultValue();
        }
        return attr.getValue();
    }

    public double getRegenSpeed() {
        var attr = this.provider.getAttributeInstance(ModAttributes.MANA_REGEN_SPEED);
        if (attr == null) {
            return ModAttributes.MANA_REGEN_SPEED.getDefaultValue();
        }
        return attr.getValue();
    }

    public double getHealthMultiplier() {
        var attr = this.provider.getAttributeInstance(ModAttributes.MANA_HEALTH_FACTOR);
        if (attr == null) {
            return ModAttributes.MANA_HEALTH_FACTOR.getDefaultValue();
        }
        return attr.getValue();
    }

    @Override
    public String toString() {
        return "ManaComponent of " + this.provider.toString();
    }
}
