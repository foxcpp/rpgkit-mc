package com.github.sweetsnowywitch.rpgkit.magic.spell;

import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.ItemEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Aspect implements SpellElement, Comparable<Aspect> {
    public enum Kind {
        EFFECT,
        REACTION,
    }

    public static final int DEFAULT_COLOR = ColorHelper.Argb.getArgb(0xFF, 0x99, 0xD9, 0xEA); /* light blue, kinda */

    public final Identifier id;

    private final Kind kind;
    private final ImmutableMap<String, @NotNull Float> scales;
    private final int color;
    private final boolean primary;
    private final int order;
    private final Identifier texturePath;
    private final ImmutableList<ItemEffect> itemEffects;
    private final ImmutableList<AreaEffect> areaEffects;
    private final ImmutableList<UseEffect> useEffects;
    private final ImmutableList<SpellReaction> reactions;
    private final @Nullable SpellForm preferredUseForm;
    private final int preferredFormWeight;

    public Aspect(Identifier id, boolean primary, JsonObject obj) {
        this.id = id;
        this.kind = Aspect.Kind.valueOf(obj.get("kind").getAsString().toUpperCase());
        this.primary = primary;

        ImmutableMap.Builder<String, Float> costs = ImmutableMap.builder();
        var costsJson = obj.getAsJsonObject("costs");
        if (costsJson != null) {
            for (var element : costsJson.entrySet()) {
                costs.put(element.getKey(), element.getValue().getAsFloat());
            }
        }
        this.scales = costs.build();

        if (obj.has("color")) {
            this.color = Integer.parseInt(obj.get("color").getAsString(), 16);
        } else {
            this.color = DEFAULT_COLOR;
        }

        int order = 0;
        if (obj.has("order")) {
            order = obj.get("order").getAsInt();
        }
        this.order = order;

        ImmutableList.Builder<ItemEffect> itemEffects = ImmutableList.builder();
        if (obj.has("item_effects")) {
            for (JsonElement effectElement : obj.getAsJsonArray("item_effects")) {
                itemEffects.add(ItemEffect.fromJson(effectElement.getAsJsonObject()));
            }
        }
        this.itemEffects = itemEffects.build();

        ImmutableList.Builder<AreaEffect> areaEffects = ImmutableList.builder();
        if (obj.has("area_effects")) {
            for (JsonElement effectElement : obj.getAsJsonArray("area_effects")) {
                areaEffects.add(AreaEffect.fromJson(effectElement.getAsJsonObject()));
            }
        }
        this.areaEffects = areaEffects.build();

        ImmutableList.Builder<UseEffect> useEffects = ImmutableList.builder();
        if (obj.has("use_effects")) {
            for (JsonElement effectElement : obj.getAsJsonArray("use_effects")) {
                useEffects.add(UseEffect.fromJson(effectElement.getAsJsonObject()));
            }
        }
        this.useEffects = useEffects.build();

        ImmutableList.Builder<SpellReaction> reactions = ImmutableList.builder();
        if (obj.has("reactions")) {
            for (JsonElement reactionEl : obj.getAsJsonArray("reactions")) {
                reactions.add(SpellReaction.fromJson(reactionEl.getAsJsonObject()));
            }
        }
        this.reactions = reactions.build();

        SpellForm preferredForm = null;
        int preferredFormWeight = 0;
        if (obj.has("use_form")) {
            var formJson = obj.getAsJsonObject("use_form");
            preferredForm = MagicRegistries.FORMS.get(new Identifier(formJson.get("id").getAsString()));
            preferredFormWeight = formJson.get("weight").getAsInt();
        }
        this.preferredUseForm = preferredForm;
        this.preferredFormWeight = preferredFormWeight;

        this.texturePath = new Identifier(id.getNamespace(), "textures/magic/aspects/" + id.getPath() + ".png");
    }

    public Kind getKind() {
        return kind;
    }

    public float getBaseCost(String key) {
        return Objects.requireNonNull(scales.getOrDefault(key, (float) 0));
    }

    public int getColor() {
        return this.color;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    @Override
    public ImmutableList<ItemEffect> itemEffects() {
        return this.itemEffects;
    }

    @Override
    public ImmutableList<AreaEffect> areaEffects() {
        return this.areaEffects;
    }

    @Override
    public ImmutableList<UseEffect> useEffects() {
        return this.useEffects;
    }

    @Override
    public ImmutableList<SpellReaction> globalReactions() {
        return this.reactions;
    }

    public Identifier getTexturePath() {
        return this.texturePath;
    }

    @Override
    public @Nullable SpellForm getPreferredForm() {
        return this.preferredUseForm;
    }

    @Override
    public int getPreferredFormWeight() {
        return this.preferredFormWeight;
    }

    @Override
    public String toString() {
        return "Aspect[id=" + id + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aspect aspect = (Aspect) o;
        return id.equals(aspect.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(@NotNull Aspect o) {
        var ord = this.order - o.order;
        if (ord == 0) {
            return this.id.compareTo(o.id);
        }
        return ord;
    }

    public static Aspect fromNbt(NbtCompound comp) {
        var id = Identifier.tryParse(comp.getString("Id"));
        if (id == null) {
            throw new IllegalStateException("Malformed aspect ID in NBT: %s".formatted(comp.getString("Id")));
        }
        var asp = MagicRegistries.ASPECTS.get(id);
        if (asp == null) {
            throw new IllegalArgumentException("Unknown aspect ID: %s".formatted(id.toString()));
        }
        return asp;
    }

    @Override
    public void writeToNbt(NbtCompound comp) {
        comp.putString("Type", "Aspect");
        comp.putString("Id", this.id.toString());
    }
}
