package com.github.foxcpp.rpgkitmc.magic.effects.item;

import com.github.foxcpp.rpgkitmc.JsonHelpers;
import com.github.foxcpp.rpgkitmc.magic.effects.ItemEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CasterApplyEffect extends ItemEffect {
    public class Used extends ItemEffect.Used {
        protected final ImmutableList<UseEffect.Used> effects;

        protected Used(SpellBuildCondition.Context ctx) {
            super(CasterApplyEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            this.effects = CasterApplyEffect.this.effects.stream().
                    filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).
                    collect(ImmutableList.toImmutableList());
            for (var effect : this.effects) {
                this.globalReactions.addAll(effect.getGlobalReactions());
            }
        }

        protected Used(JsonObject obj) {
            super(CasterApplyEffect.this, obj);
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect.Used::fromJson);
        }

        @Override
        public @NotNull TypedActionResult<ItemStack> useOnItem(ServerSpellCast cast, ServerWorld world, ItemStack stack, @Nullable Inventory container, @Nullable Entity holder) {
            var caster = cast.getCaster(world);
            if (caster == null) {
                return TypedActionResult.pass(stack);
            }

            var lastResult = ActionResult.PASS;
            boolean success = false;
            for (var eff : this.effects) {
                lastResult = eff.useOnEntity(cast, caster);
                if (lastResult.equals(ActionResult.SUCCESS)) {
                    success = true;
                }
                if (lastResult.equals(ActionResult.CONSUME) || lastResult.equals(ActionResult.FAIL)) {
                    return new TypedActionResult<>(lastResult, stack);
                }
            }

            if (success) {
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("effects", JsonHelpers.toJsonList(this.effects));
        }
    }

    protected final ImmutableList<UseEffect> effects;

    protected CasterApplyEffect(Identifier id) {
        super(id);
        this.effects = ImmutableList.of();
    }

    protected CasterApplyEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("effects")) {
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect::fromJson);
        } else {
            this.effects = ImmutableList.of();
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
        obj.add("effects", JsonHelpers.toJsonList(this.effects));
    }
}
