package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SpellElement {
    String COST_REGENERATIO = "regeneratio";
    String COST_MUTATIO = "mutatio";
    String COST_INTERITIO = "interitio";
    String COST_MAGICAE = "magicae";
    ImmutableList<String> COST_BALANCE = ImmutableList.of(COST_REGENERATIO, COST_MUTATIO, COST_INTERITIO);
    ImmutableList<String> COST_ALL = ImmutableList.of(COST_MAGICAE, COST_REGENERATIO, COST_MUTATIO, COST_INTERITIO);

    float getBaseCost(String key);

    int getColor();

    static SpellElement of(Aspect asp) {
        return asp;
    }
    @Contract(value = "_ -> new", pure = true)
    static @NotNull SpellElement of(Item item) {
        return new ItemElement(item);
    }
}
