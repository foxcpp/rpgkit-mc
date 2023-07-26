package com.github.foxcpp.rpgkitmc.magic.spell;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SpellRecipeMap<T> {
    public record Element(Aspect aspect, Ingredient item, boolean consume) {
        public static Element fromJson(JsonObject obj) throws IllegalArgumentException {
            if (obj.has("aspect")) {
                var aspectId = new Identifier(obj.get("aspect").getAsString());
                var aspect = MagicRegistries.ASPECTS.get(aspectId);
                if (aspect == null) {
                    throw new IllegalArgumentException("attempting to unserialize an unregistered aspect in spell element: %s".formatted(aspectId.toString()));
                }
                return new Element(aspect, null, false);
            }

            var ingredient = Ingredient.fromJson(obj);
            var consume = false;
            if (obj.has("consume")) {
                consume = obj.get("consume").getAsBoolean();
            }
            return new Element(null, ingredient, consume);
        }

        public JsonObject toJson() throws IllegalStateException {
            if (aspect != null) {
                Identifier aspectId = null;
                for (var entry : MagicRegistries.ASPECTS.entrySet()) {
                    if (entry.getValue().equals(this.aspect)) {
                        aspectId = entry.getKey();
                        break;
                    }
                }
                if (aspectId == null) {
                    throw new IllegalArgumentException("attempting to serialize an unregistered aspect in spell element: %s".formatted(aspect.toString()));
                }
                var res = new JsonObject();
                res.addProperty("aspect", aspectId.toString());
                return res;
            } else if (item != null) {
                return item.toJson().getAsJsonObject();
            } else {
                throw new IllegalStateException("malformed element");
            }
        }
    }

    public record Recipe<T>(@NotNull ImmutableList<Element> elements, @NotNull T result) {
    }

    private final List<Recipe<T>> recipes = new ArrayList<>();

    public void addRecipe(@NotNull ImmutableList<Element> elements, @NotNull T result) {
        recipes.add(new Recipe<>(elements, result));
    }

    public void copyFrom(@NotNull SpellRecipeMap<T> from) {
        this.recipes.addAll(from.recipes);
    }

    public void clear() {
        this.recipes.clear();
    }

    public @Nullable Recipe<T> tryMatch(List<SpellElement> elements) {
        return this.tryMatch(elements, null);
    }

    public @Nullable Recipe<T> tryMatch(List<SpellElement> elements, @Nullable Predicate<T> includeOnly) {
        var res = this.tryMatchInner(elements, includeOnly, false);
        if (res.size() == 0) return null;
        return res.get(0);
    }

    public List<Recipe<T>> tryMatchMultiple(List<SpellElement> elements, @Nullable Predicate<T> includeOnly) {
        return this.tryMatchInner(elements, includeOnly, true);
    }

    public List<Recipe<T>> tryMatchMultiple(List<SpellElement> elements) {
        return this.tryMatchInner(elements, null, true);
    }

    private List<Recipe<T>> tryMatchInner(List<SpellElement> elements, @Nullable Predicate<T> includeOnly, boolean multiple) {
        var res = new ArrayList<Recipe<T>>();
        for (Recipe<T> recipe : recipes) {
            if (includeOnly != null && !includeOnly.test(recipe.result)) {
                continue;
            }

            if (recipe.elements.size() != elements.size()) {
                continue;
            }
            boolean matched = true;
            for (int i = 0; i < recipe.elements.size(); i++) {
                var comboElement = recipe.elements.get(i);
                var element = elements.get(i);

                if (comboElement.aspect != null) {
                    if (!(element instanceof Aspect asp)) {
                        matched = false;
                        break;
                    }
                    if (!asp.equals(comboElement.aspect)) {
                        matched = false;
                        break;
                    }
                } else if (comboElement.item != null) {
                    if (!(element instanceof ItemElement ise)) {
                        matched = false;
                        break;
                    }
                    if (!comboElement.item.test(new ItemStack(ise.getItem()))) {
                        matched = false;
                        break;
                    }
                }
            }
            if (!matched) {
                continue;
            }

            if (!multiple) {
                return List.of(recipe);
            }
            res.add(recipe);
        }
        return res;
    }
}
