package com.github.sweetsnowywitch.csmprpgkit.magic;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@FunctionalInterface
public interface SpellBuildCondition {
    boolean shouldAdd(SpellBuilder builder, @Nullable SpellElement element);

    static SpellBuildCondition fromJson(JsonObject obj) {
        SpellBuildCondition cond = null;

        for (var el : obj.entrySet()) {
            SpellBuildCondition elCond;
            switch (el.getKey()) {
                case "not" -> elCond = not(fromJson(el.getValue().getAsJsonObject()));
                case "and" -> {
                    var arr = el.getValue().getAsJsonArray();
                    if (arr.size() != 2) {
                        throw new IllegalArgumentException("and condition should have exactly 2 elements");
                    }
                    elCond = and(fromJson(arr.get(0).getAsJsonObject()), fromJson(arr.get(1).getAsJsonObject()));
                }
                case "or" -> {
                    var arr = el.getValue().getAsJsonArray();
                    if (arr.size() != 2) {
                        throw new IllegalArgumentException("or condition should have exactly 2 elements");
                    }
                    elCond = or(fromJson(arr.get(0).getAsJsonObject()), fromJson(arr.get(1).getAsJsonObject()));
                }
                case "first_element" -> {
                    if (el.getValue().getAsBoolean()) {
                        elCond = sameAsFirstElement();
                    } else {
                        elCond = not(sameAsFirstElement());
                    }
                }
                default -> throw new IllegalArgumentException("unknown field in spell build condition: " + el.getKey());
            }

            if (cond == null) {
                cond = elCond;
            } else {
                cond = and(cond, elCond);
            }
        }

        return cond;
    }

    static SpellBuildCondition not(SpellBuildCondition c) {
        return (builder, el) -> !c.shouldAdd(builder, el);
    }

    static SpellBuildCondition and(SpellBuildCondition c1, SpellBuildCondition c2) {
        return (builder, el) -> c1.shouldAdd(builder, el) && c2.shouldAdd(builder, el);
    }

    static SpellBuildCondition or(SpellBuildCondition c1, SpellBuildCondition c2) {
        return (builder, el) -> c1.shouldAdd(builder, el) || c2.shouldAdd(builder, el);
    }

    static SpellBuildCondition firstElement() {
        return (builder, el) -> builder.getPendingElements().size() == 0;
    }

    static SpellBuildCondition sameAsFirstElement() {
        return (builder, el) -> builder.getPendingElements().size() == 0 || builder.getPendingElements().get(0).equals(el);
    }

    static SpellBuildCondition hasElement(@NotNull SpellElement element, @Range(from = 1, to = Integer.MAX_VALUE) int minCount, int maxCount) {
        return (builder, el) -> {
            int effectiveCnt = 0;
            for (var pending : builder.getPendingElements()) {
                if (pending.equals(element)) {
                    effectiveCnt++;
                }
            }

            return effectiveCnt >= minCount && effectiveCnt <= maxCount;
        };
    }

    static SpellBuildCondition casterIsPlayer() {
        return (builder, el) -> builder.getCaster() instanceof PlayerEntity;
    }
}
