package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.ItemSelectorMapping;
import com.github.foxcpp.rpgkitmc.magic.json.BlockStatePredicate;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InsertItemEffect extends SimpleUseEffect {
    protected final ItemSelectorMapping mapping;
    protected final @Nullable BlockStatePredicate filter;
    protected final int slot;
    protected final boolean replace;
    protected final int maxCount;

    protected InsertItemEffect(Identifier id) {
        super(id);
        this.mapping = null;
        this.filter = null;
        this.slot = 0;
        this.replace = false;
        this.maxCount = 1;
    }

    protected InsertItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        this.mapping = new ItemSelectorMapping(obj.getAsJsonArray("entries"));
        if (obj.has("filter")) {
            this.filter = new BlockStatePredicate(obj.get("filter"));
        } else {
            this.filter = null;
        }
        if (obj.has("slot")) {
            this.slot = obj.get("slot").getAsInt();
        } else {
            throw new IllegalArgumentException("slot field is required for InsertItemEffect");
        }
        this.replace = obj.has("replace") && obj.get("replace").getAsBoolean();
        if (obj.has("max_count")) {
            this.maxCount = obj.get("max_count").getAsInt();
        } else {
            this.maxCount = 1;
        }
    }

    @Override
    protected @NotNull ActionResult useOnBlock(ServerSpellCast cast, Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        var be = world.getBlockEntity(pos);
        if (!(be instanceof Inventory inv)) {
            return ActionResult.PASS;
        }

        var lcb = new LootContextParameterSet.Builder(world);
        lcb.luck(0.5f);
        lcb.add(LootContextParameters.ORIGIN, cast.getOriginPos());
        var thisEntity = cast.getCaster(world);
        if (thisEntity == null) {
            RPGKitMagicMod.LOGGER.warn("Could not provide THIS_ENTITY for loot functions");
        }
        lcb.add(LootContextParameters.THIS_ENTITY, thisEntity);
        var context = (new LootContext.Builder(lcb.build(LootContextTypes.SELECTOR))).build(new Identifier(RPGKitMagicMod.MOD_ID, "random"));

        var stack = this.mapping.apply(ItemStack.EMPTY, context);

        var current = inv.getStack(this.slot);
        if (!current.isEmpty() && !current.isOf(stack.getItem()) && !this.replace) {
            return ActionResult.PASS;
        }
        if (current.isOf(stack.getItem())) {
            if (stack.getCount() + current.getCount() > this.maxCount) {
                return ActionResult.PASS;
            }

            current.increment(stack.getCount());
            inv.setStack(this.slot, current);
        } else {
            inv.setStack(this.slot, stack);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.add("entries", this.mapping.toJsonArray());
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
        obj.addProperty("slot", this.slot);
        obj.addProperty("replace", this.replace);
        obj.addProperty("max_count", this.maxCount);
    }
}
