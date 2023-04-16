package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SpellElement {
    String COST_REGENERATIO = "regeneratio";
    String COST_MUTATIO = "mutatio";
    String COST_INTERITIO = "interitio";
    String COST_MAGICAE = "magicae";
    ImmutableList<String> COST_BALANCE = ImmutableList.of(COST_REGENERATIO, COST_MUTATIO, COST_INTERITIO);
    ImmutableList<String> COST_ALL = ImmutableList.of(COST_MAGICAE, COST_REGENERATIO, COST_MUTATIO, COST_INTERITIO);

    float getBaseCost(String key);

    int getColor();

    /**
     * Called when SpellElement is consumed during spell completion.
     */
    default void consume() {}

    /**
     * Called to check whether the user is still able to use the element.
     * Should recheck the same requirements that are checked when element was added.
     */
    default boolean isValid(@Nullable LivingEntity user) { return true; }

    static SpellElement of(Aspect asp) {
        return asp;
    }
    @Contract(value = "_ -> new", pure = true)
    static @NotNull SpellElement of(Item item) {
        return new ItemElement(item);
    }

    static SpellElement readFromNbt(NbtCompound comp) {
        return switch (comp.getString("Type")) {
            case "Aspect" -> Aspect.fromNbt(comp);
            case "Item" -> ItemElement.fromNbt(comp);
            default -> throw new IllegalArgumentException("Unknown SpellElement type: %s".formatted(comp.getString("Type")));
        };
    }

    void writeToNbt(NbtCompound comp);
}
