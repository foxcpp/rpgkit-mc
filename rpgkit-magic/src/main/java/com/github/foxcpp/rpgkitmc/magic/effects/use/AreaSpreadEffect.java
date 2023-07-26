package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.JsonHelpers;
import com.github.foxcpp.rpgkitmc.magic.effects.AreaEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.json.FloatModifier;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AreaSpreadEffect extends UseEffect {
    public static class Reaction extends SpellReaction {
        protected final FloatModifier radius;

        protected Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("radius")) {
                this.radius = new FloatModifier(obj.get("radius"));
            } else {
                this.radius = FloatModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof AreaSpreadEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("radius", this.radius.toJson());
        }
    }

    public class Used extends UseEffect.Used {
        private final ImmutableList<AreaEffect.Used> effects;
        private final float radius;

        protected Used(SpellBuildCondition.Context ctx) {
            super(AreaSpreadEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);

            this.effects = AreaSpreadEffect.this.effects.stream().
                    filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).
                    collect(ImmutableList.toImmutableList());
            for (var effect : this.effects) {
                this.globalReactions.addAll(effect.getGlobalReactions());
            }

            var radius = AreaSpreadEffect.this.radius;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    radius = r.radius.applyMultiple(radius, ctx.stackSize);
                }
            }
            for (var reaction : this.getGlobalReactions()) {
                if (reaction instanceof Reaction r) {
                    radius = r.radius.applyMultiple(radius, ctx.stackSize);
                }
            }
            this.radius = radius;
        }

        protected Used(JsonObject obj) {
            super(AreaSpreadEffect.this, obj);
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), AreaEffect.Used::fromJson);
            this.radius = obj.get("radius").getAsFloat();
        }

        @Override
        public @NotNull ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            var center = Vec3d.ofCenter(pos);
            var area = Box.of(center, this.radius, this.radius, this.radius);

            var lastResult = ActionResult.PASS;
            boolean success = false;
            for (var effect : this.effects) {
                lastResult = effect.useOnArea(cast, world,
                        area, center, AreaEffect.AreaCollider.cube(area));
                if (lastResult.equals(ActionResult.SUCCESS)) {
                    success = true;
                }
                if (lastResult.equals(ActionResult.CONSUME) || lastResult.equals(ActionResult.FAIL)) {
                    return lastResult;
                }
            }
            
            if (lastResult.equals(ActionResult.PASS) && success) {
                return ActionResult.SUCCESS;
            }
            return lastResult;
        }

        @Override
        public @NotNull ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            var area = Box.of(entity.getPos(), this.radius, this.radius, this.radius);

            var lastResult = ActionResult.PASS;
            for (var effect : this.effects) {
                lastResult = effect.useOnArea(cast, (ServerWorld) entity.getWorld(),
                        area, entity.getPos(), AreaEffect.AreaCollider.cube(area));
                if (lastResult.equals(ActionResult.FAIL) || lastResult.equals(ActionResult.CONSUME)) {
                    break;
                }
            }
            return lastResult;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("effects", JsonHelpers.toJsonList(this.effects));
            obj.addProperty("radius", this.radius);
        }
    }

    protected final float radius;
    protected final ImmutableList<AreaEffect> effects;

    protected AreaSpreadEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("radius")) {
            this.radius = obj.get("radius").getAsFloat();
        } else {
            this.radius = 1;
        }
        if (obj.has("effects")) {
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), AreaEffect::fromJson);
        } else {
            this.effects = ImmutableList.of();
        }
    }

    @Override
    public UseEffect.@NotNull Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public UseEffect.@NotNull Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("radius", this.radius);
        obj.add("effects", JsonHelpers.toJsonList(this.effects));
    }
}
