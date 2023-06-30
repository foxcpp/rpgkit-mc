package com.github.sweetsnowywitch.rpgkit.magic.spell;

import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.ItemEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface SpellElement permits Aspect, ItemElement {
    String COST_REGENERATIO = "regeneratio";
    String COST_MUTATIO = "mutatio";
    String COST_INTERITIO = "interitio";
    String COST_MAGICAE = "magicae";
    ImmutableList<String> COST_BALANCE = ImmutableList.of(COST_REGENERATIO, COST_MUTATIO, COST_INTERITIO);
    ImmutableList<String> COST_ALL = ImmutableList.of(COST_MAGICAE, COST_REGENERATIO, COST_MUTATIO, COST_INTERITIO);

    float getBaseCost(String key);

    int getColor();

    default ImmutableList<ItemEffect> itemEffects() {
        return ImmutableList.of();
    }

    default ImmutableList<AreaEffect> areaEffects() {
        return ImmutableList.of();
    }

    default ImmutableList<UseEffect> useEffects() {
        return ImmutableList.of();
    }

    default ImmutableList<SpellReaction> formReactions() {
        return ImmutableList.of();
    }

    default @Nullable SpellForm getPreferredForm() {
        return null;
    }

    default int getPreferredFormWeight() {
        return 0;
    }

    /**
     * Called when SpellElement is consumed during spell completion.
     */
    default void consume() {
    }

    /**
     * Called to check whether the user is still able to use the element.
     * Should recheck the same requirements that are checked when element was added.
     */
    default boolean isValid(@Nullable LivingEntity user) {
        return true;
    }

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
            default ->
                    throw new IllegalArgumentException("Unknown SpellElement type: %s".formatted(comp.getString("Type")));
        };
    }

    void writeToNbt(NbtCompound comp);

    static int calculateBaseColor(List<SpellElement> elements) {
        int[] elementColors = new int[3];
        int i = 0;

        for (var element : elements) {
            if (element instanceof Aspect) {
                elementColors[i] = element.getColor() + 0x10000000;
                i++;
                while (i >= 2) {
                    var calculatedColor = ColorHelper.Argb.mixColor(elementColors[0], elementColors[1]);
                    elementColors[1] = 0;
                    i = 1;
                    elementColors[0] = calculatedColor;
                }
            }
        }
        return elementColors[0];
    }
}
