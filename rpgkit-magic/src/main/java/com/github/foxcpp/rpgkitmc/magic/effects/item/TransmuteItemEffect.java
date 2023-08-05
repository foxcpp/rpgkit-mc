package com.github.foxcpp.rpgkitmc.magic.effects.item;

import com.github.foxcpp.rpgkitmc.magic.MagicRegistries;
import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.ItemMapping;
import com.github.foxcpp.rpgkitmc.magic.effects.ItemEffect;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class TransmuteItemEffect extends ItemEffect {
    protected final ItemMapping mapping;
    private final String mappingId;
    protected final boolean preserveCount;

    protected TransmuteItemEffect(Identifier id) {
        super(id);
        this.mapping = null;
        this.preserveCount = false;
        this.mappingId = null;

        RPGKitMagicMod.LOGGER.warn("TransformItemEffect is not configured, will be no-op");
    }

    protected TransmuteItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (!obj.has("mapping")) {
            throw new IllegalArgumentException("missing mapping field for TransmuteItemEffect");
        }

        this.mapping = MagicRegistries.TRANSMUTE_MAPPINGS.get(new Identifier(obj.get("mapping").getAsString()));
        if (this.mapping == null) {
            throw new IllegalArgumentException("transmute mapping does not exist: " + obj.get("mapping").getAsString());
        }
        this.mappingId = obj.get("mapping").getAsString();

        this.preserveCount = obj.has("preserve_count") && obj.get("preserve_count").getAsBoolean();
    }

    public class Used extends ItemEffect.Used {
        protected Used(SpellBuildCondition.Context ctx) {
            super(TransmuteItemEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
        }

        protected Used(JsonObject obj) {
            super(TransmuteItemEffect.this, obj);
        }

        @Override
        public @NotNull TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
            if (TransmuteItemEffect.this.mapping == null) {
                return TypedActionResult.pass(stack);
            }

            if (stack.isEmpty()) {
                return TypedActionResult.pass(stack);
            }
            var originalCount = stack.getCount();

            var lcb = new LootContextParameterSet.Builder(world);
            lcb.luck(0.5f);
            lcb.add(LootContextParameters.ORIGIN, cast.getOriginPos());
            Entity thisEntity;
            if (holder != null) {
                thisEntity = holder;
            } else {
                thisEntity = cast.getCaster(world);
                if (thisEntity == null) {
                    RPGKitMagicMod.LOGGER.warn("Could not provide THIS_ENTITY for loot functions");
                }
            }
            lcb.add(LootContextParameters.THIS_ENTITY, thisEntity);
            var context = (new LootContext.Builder(lcb.build(LootContextTypes.SELECTOR))).build(new Identifier(RPGKitMagicMod.MOD_ID, "random"));

            var res = TransmuteItemEffect.this.mapping.apply(stack, context);
            if (TransmuteItemEffect.this.preserveCount) {
                res.setCount(originalCount);
            }

            return TypedActionResult.success(res);
        }
    }

    @Override
    public @NotNull Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public @NotNull Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("mapping", this.mappingId);
        obj.addProperty("preserve_count", this.preserveCount);
    }
}
