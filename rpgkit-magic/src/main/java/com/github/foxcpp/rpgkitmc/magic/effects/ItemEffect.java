package com.github.foxcpp.rpgkitmc.magic.effects;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ItemEffect extends SpellEffect {
    protected ItemEffect(Identifier id) {
        super(id);
    }

    protected ItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);
    }

    @FunctionalInterface
    public interface JsonFactory {
        ItemEffect createEffectFromJSON(Identifier id, JsonObject obj);
    }

    public static abstract class Used extends SpellEffect.Used<ItemEffect> {
        protected Used(ItemEffect effect, List<SpellReaction> globalReactions, List<SpellReaction> appliedReactions, SpellBuildCondition.Context ctx) {
            super(effect, globalReactions, appliedReactions, ctx);
        }

        protected Used(ItemEffect effect, JsonObject obj) {
            super(effect, obj);
        }

        @NotNull
        public abstract TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder);

        public static ItemEffect.Used fromJson(JsonObject obj) {
            var effect = ItemEffect.fromJson(obj.getAsJsonObject("effect"));
            return effect.usedFromJson(obj);
        }
    }

    @NotNull
    public abstract Used use(SpellBuildCondition.Context ctx);

    @NotNull
    public abstract Used usedFromJson(JsonObject obj);

    public static ItemEffect fromJson(JsonObject obj) {
        var type = obj.get("type");
        if (type == null) {
            throw new IllegalArgumentException("missing type field in spell effect definition");
        }
        var effectId = new Identifier(type.getAsString());
        var effect = MagicRegistries.ITEM_EFFECTS.get(effectId);
        if (effect == null) {
            throw new IllegalArgumentException("unknown effect: %s".formatted(effectId.toString()));
        }
        return effect.createEffectFromJSON(effectId, obj);
    }
}
