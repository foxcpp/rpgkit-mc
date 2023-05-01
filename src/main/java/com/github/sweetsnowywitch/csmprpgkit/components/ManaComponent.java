package com.github.sweetsnowywitch.csmprpgkit.components;

import com.github.sweetsnowywitch.csmprpgkit.ModAttributes;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;

public class ManaComponent implements AutoSyncedComponent, ServerTickingComponent {
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
        if (this.value < maxValue && RPGKitMod.RANDOM.nextFloat() <= speed)
            this.value += regen;
        else if (this.value > maxValue)
            this.value = maxValue;
        ModComponents.MANA.sync(this.provider);
    }

    public void spendMana(double cost){
        this.value -= cost;
        if (this.value < 0) {
            var damage = (float)(-this.value * this.getHealthMultiplier());
            if (damage > this.provider.getHealth() - 0.5f) {
                damage = this.provider.getHealth() - 0.5f;
            }
            if (this.provider.getHealth() <= 1) {
                damage = 10;
            }
            this.provider.damage(DamageSource.MAGIC, damage);
            this.value = 0;
        }
        ModComponents.MANA.sync(this.provider);
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
}
