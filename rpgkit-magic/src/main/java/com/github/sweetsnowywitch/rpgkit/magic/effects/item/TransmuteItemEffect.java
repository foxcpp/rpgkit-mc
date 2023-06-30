package com.github.sweetsnowywitch.rpgkit.magic.effects.item;

import com.github.sweetsnowywitch.rpgkit.magic.ItemTransmuteMapping;
import com.github.sweetsnowywitch.rpgkit.magic.MagicRegistries;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.ItemEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class TransmuteItemEffect extends ItemEffect {
    protected final ItemTransmuteMapping mapping;

    protected TransmuteItemEffect(Identifier id) {
        super(id);

        this.mapping = null;

        RPGKitMagicMod.LOGGER.warn("TransformItemEffect is not configured, will be no-op");
    }

    protected TransmuteItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        this.mapping = MagicRegistries.TRANSMUTE_MAPPINGS.get(new Identifier(obj.get("mapping").getAsString()));
        if (this.mapping == null) {
            throw new IllegalArgumentException("transmute mapping does not exist");
        }
    }

    public class Used extends ItemEffect.Used {
        protected Used(SpellBuildCondition.Context ctx) {
            super(TransmuteItemEffect.this, new ArrayList<>(), ctx);
        }

        protected Used(JsonObject obj) {
            super(TransmuteItemEffect.this, obj);
        }

        @Override
        public TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
            if (TransmuteItemEffect.this.mapping == null) {
                return TypedActionResult.pass(stack);
            }

            if (stack.isEmpty()) {
                return TypedActionResult.pass(stack);
            }

            var lcb = new LootContext.Builder(world);
            lcb.luck(0.5f);
            lcb.parameter(LootContextParameters.ORIGIN, cast.getOriginPos());
            Entity thisEntity;
            if (holder != null) {
                thisEntity = holder;
            } else {
                thisEntity = cast.getCaster(world);
                if (thisEntity == null) {
                    RPGKitMagicMod.LOGGER.warn("Could not provide THIS_ENTITY for loot functions");
                }
            }
            lcb.parameter(LootContextParameters.THIS_ENTITY, thisEntity);
            var context = lcb.build(LootContextTypes.SELECTOR);

            return TypedActionResult.success(TransmuteItemEffect.this.mapping.transmute(stack, context));
        }
    }

    @Override
    public Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }
}
