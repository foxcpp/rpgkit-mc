package com.github.sweetsnowywitch.csmprpgkit.components;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;

public class ManaComponent implements AutoSyncedComponent, ServerTickingComponent {
    private final LivingEntity provider;
    private int value;
    private int maxValue;
    private int regen;
    private float regenSpeed;
    private float healthMultiplier;

    public ManaComponent(LivingEntity provider) {
        this.provider = provider;
        this.value = 20;
        this.maxValue = 20;
        this.regen = 1;
        this.regenSpeed = 0.005f;
        this.healthMultiplier = 1.0f;
    }

    @Override
    public void serverTick() {
        regenerate(this.regen, this.regenSpeed);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.value = tag.getInt("manaValue");
        this.maxValue = tag.getInt("maxMana");
        this.regen = tag.getInt("manaReg");
        this.regenSpeed = tag.getFloat("manaSpeed");
        this.healthMultiplier = tag.getFloat("manaMultiplier");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("manaValue", this.value);
        tag.putInt("maxMana", this.maxValue);
        tag.putInt("manaReg", this.regen);
        tag.putFloat("manaSpeed", this.regenSpeed);
        tag.putFloat("manaMultiplier", this.healthMultiplier);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player.equals(this.provider);
    }

    public void regenerate(int reg, float speed) {
        if (this.value < this.maxValue && new Random().nextFloat() <= speed)
            this.value += reg;
        else if (this.value > this.maxValue)
            this.value = this.maxValue;
        ModComponents.MANA.sync(this.provider);
    }

    public void spendMana(int cost){
        this.value -= cost;
        if (this.value < 0)
        {
            this.provider.damage(DamageSource.MAGIC, -this.value * healthMultiplier);
            //this.provider.world.addParticle(ParticleTypes.ELECTRIC_SPARK,
            //        this.provider.getX() + new Random().nextDouble(-0.5,0.5),
            //        this.provider.getY() + new Random().nextDouble(0.5,2),
            //        this.provider.getZ() + new Random().nextDouble(-0.5,0.5),
            //        0, 0, 0);
            this.value = 0;
        }
        ModComponents.MANA.sync(this.provider);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        ModComponents.MANA.sync(this.provider);
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        ModComponents.MANA.sync(this.provider);
    }

    public int getRegen() {
        return regen;
    }

    public void setRegen(int regen) {
        this.regen = regen;
        ModComponents.MANA.sync(this.provider);
    }

    public float getRegenSpeed() {
        return regenSpeed;
    }

    public void setRegenSpeed(int regenSpeed) {
        this.regenSpeed = regenSpeed;
        ModComponents.MANA.sync(this.provider);
    }

    public float getHealthMultiplier() {
        return healthMultiplier;
    }

    public void setHealthMultiplier(float healthMultiplier) {
        this.healthMultiplier = healthMultiplier;
        ModComponents.MANA.sync(this.provider);
    }
}
