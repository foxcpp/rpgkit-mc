package com.github.sweetsnowywitch.csmprpgkit.components;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.classes.CharacterClass;
import com.github.sweetsnowywitch.csmprpgkit.classes.Perk;
import com.github.sweetsnowywitch.csmprpgkit.classes.ServerTickablePerk;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassComponent holds current per-class levels, total character levels and modded experience
 * points. It depends on all its providers also having AbilitiesComponent since
 * classes affect abilities.
 */
public class ClassComponent implements AutoSyncedComponent, ComponentV3, ServerTickingComponent {
    public static final int[] REQUIRED_LEVEL_EXP = { // TODO: Make configurable.
            0, // 0 lvl
            10, // 1 lvl
            20, 20, 20, 20, // 2-5 lvl
            30, 30, 30, 30, 30, // 6-10 lvl
            40, 40, 40, 40, 40, // 11-15 lvl
            50, 50, 50, 50, 50 // 16-20 lvl
    };

    private final LivingEntity provider;
    private int currentLevelExp;
    private int currentLevel;
    private int undistributedLevels;
    private final Map<Identifier, Integer> classLevels;

    private final Map<Identifier, ArrayList<Perk>> perks; // cache, computed from classLevels on load/update.
    private final ArrayList<ServerTickablePerk> tickablePerks; // cached, computed from classLevels on load/update

    public ClassComponent(LivingEntity provider) {
        this.provider = provider;

        this.currentLevelExp = 0;
        this.currentLevel = 0;
        this.undistributedLevels = 0;
        this.classLevels = new HashMap<>();

        this.perks = new HashMap<>();
        this.tickablePerks = new ArrayList<>();
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.currentLevelExp = tag.getInt("CurrentLevelExp");
        this.currentLevel = tag.getInt("CurrentLevel");
        this.undistributedLevels = tag.getInt("UndistributedLevels");

        this.classLevels.clear();
        var classLevels = tag.getCompound("ClassLevels");
        for (var klass : classLevels.getKeys()) {
            var klassId = Identifier.tryParse(klass);
            if (klassId == null) throw new IllegalArgumentException("malformed id in nbt: %s".formatted(klass));
            this.classLevels.put(klassId, classLevels.getInt(klass));
        }

        this.precomputePerks();
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("CurrentLevelExp", this.currentLevelExp);
        tag.putInt("CurrentLevel", this.currentLevel);
        tag.putInt("UndistributedLevels", this.undistributedLevels);
        var classLevels = new NbtCompound();
        for (var ent : this.classLevels.entrySet()) {
            classLevels.putInt(ent.getKey().toString(), ent.getValue());
        }
        tag.put("ClassLevels", classLevels);
    }

    private void precomputePerks() {
        this.perks.clear();
        this.tickablePerks.clear();
        for (var klassLevel : this.classLevels.entrySet()) {
            var klassData = ModRegistries.CLASSES.get(klassLevel.getKey());
            if (klassData == null) {
                RPGKitMod.LOGGER.warn("unknown class {} referenced for player {}, ignoring", klassLevel.getKey(), this.provider);
                continue;
            }

            for (var level : klassData.levels) {
                if (level.level <= klassLevel.getValue()) {
                    for (var perk : level.perks) {
                        this.perks.computeIfAbsent(perk.typeId, k -> new ArrayList<>()).add(perk);
                        if (perk instanceof ServerTickablePerk stp) {
                            this.tickablePerks.add(stp);
                        }
                    }
                }
            }
        }
    }

    @NotNull
    public ArrayList<Perk> getPerk(Identifier id) {
        return this.perks.getOrDefault(id, new ArrayList<>());
    }

    public int getCurrentLevelExp() {
        return this.currentLevelExp;
    }

    public int getUndistributedLevels() {
        return this.undistributedLevels;
    }

    public int getCurrentLevel() {
        return this.currentLevel;
    }

    public int getClassLevel(Identifier classId) {
        return this.classLevels.getOrDefault(classId, 0);
    }

    public Set<CharacterClass> classes() {
        return this.classLevels.keySet().stream().map(ModRegistries.CLASSES::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void resetClasses() {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot reset classes client-side");
        }

        this.classLevels.clear();
        this.undistributedLevels = this.currentLevel;

        this.precomputePerks();

        ModComponents.CLASS.sync(this.provider);
    }

    public void resetExp() {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot reset exp client-side");
        }

        this.currentLevel = 0;
        this.currentLevelExp = 0;
        this.undistributedLevels = 0;

        ModComponents.CLASS.sync(this.provider);
    }

    public void levelUp(Identifier classId) {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot level-up client-side");
        }

        var currentLevel = this.classLevels.getOrDefault(classId, 0);
        var klass = ModRegistries.CLASSES.get(classId);
        if (klass == null) {
            throw new IllegalArgumentException("attempting to level up an unknown class %s".formatted(classId.toString()));
        }
        if (this.undistributedLevels == 0) {
            throw new IllegalStateException("attempting to level up without level points");
        }

        this.classLevels.put(classId, currentLevel+1);
        this.undistributedLevels--;

        this.precomputePerks();

        ModComponents.CLASS.sync(this.provider);
    }

    public void addExp(int value) {
        if (this.provider.world.isClient) {
            throw new IllegalStateException("cannot add experience client-side");
        }

        this.currentLevelExp += value;

        while (this.currentLevel < REQUIRED_LEVEL_EXP.length-1 && this.currentLevelExp >= REQUIRED_LEVEL_EXP[this.currentLevel+1]) {
            this.currentLevelExp -= REQUIRED_LEVEL_EXP[this.currentLevel+1];
            this.undistributedLevels++;
            this.currentLevel++;
        }

        ModComponents.CLASS.sync(this.provider);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassComponent that = (ClassComponent) o;
        return currentLevelExp == that.currentLevelExp && currentLevel == that.currentLevel && undistributedLevels == that.undistributedLevels && classLevels.equals(that.classLevels);
    }

    @Override
    public void serverTick() {
        for (var perk : this.tickablePerks) {
            perk.tick((ServerPlayerEntity) this.provider);
        }
    }
}
