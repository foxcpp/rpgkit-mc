package com.github.foxcpp.rpgkitmc.magic.effects.item;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.ItemSelectorMapping;
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

public class GiveItemEffect extends ItemEffect {
    public class Used extends ItemEffect.Used {

        protected Used(SpellBuildCondition.Context ctx) {
            super(GiveItemEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
        }

        protected Used(JsonObject obj) {
            super(GiveItemEffect.this, obj);
        }

        @Override
        public @NotNull TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
            if (GiveItemEffect.this.mapping == null) {
                return TypedActionResult.pass(stack);
            }
            if (!stack.isEmpty()) {
                return TypedActionResult.pass(stack);
            }

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

            return TypedActionResult.success(GiveItemEffect.this.mapping.apply(stack, context));
        }
    }

    protected final ItemSelectorMapping mapping;

    protected GiveItemEffect(Identifier id) {
        super(id);
        this.mapping = null;
    }

    protected GiveItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        this.mapping = new ItemSelectorMapping(obj.getAsJsonArray("entries"));
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
        obj.add("entries", this.mapping.toJsonArray());
    }
}
