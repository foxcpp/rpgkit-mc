package com.github.sweetsnowywitch.csmprpgkit.components;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Random;
import java.util.logging.Logger;

public class ManaComponent implements AutoSyncedComponent, ServerTickingComponent {
    private final LivingEntity provider;
    private int value;
    private int maxValue;
    private int regen;
    private float regenSpeed;

    public ManaComponent(LivingEntity provider) {
        this.provider = provider;
        this.value = 20;
        this.maxValue = 20;
        this.regen = 1;
        this.regenSpeed = 0.005f;
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
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("manaValue", this.value);
        tag.putInt("maxMana", this.maxValue);
        tag.putInt("manaReg", this.regen);
        tag.putFloat("manaSpeed", this.regenSpeed);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.provider;
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

            var client = MinecraftClient.getInstance();

            this.value = 0;
            RPGKitMod.LOGGER.info("Not enough mana");
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
}
