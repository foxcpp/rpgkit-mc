package com.github.sweetsnowywitch.rpgkit.magic.effects.area;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.json.FloatModifier;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SurfaceSprayEffect extends AreaEffect {
    public static class Reaction extends SpellReaction {
        protected final FloatModifier areaCoverage;
        protected final FloatModifier entityCoverage;

        protected Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("area_coverage")) {
                this.areaCoverage = new FloatModifier(obj.get("area_coverage"));
            } else {
                this.areaCoverage = new FloatModifier(0.25f);
            }
            if (obj.has("entity_coverage")) {
                this.entityCoverage = new FloatModifier(obj.get("entity_coverage"));
            } else {
                this.entityCoverage = new FloatModifier(1f);
            }
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("area_coverage", this.areaCoverage.toJson());
            obj.add("entity_coverage", this.entityCoverage.toJson());
        }
    }

    public class Used extends AreaEffect.Used {
        private final ImmutableList<UseEffect.Used> effects;
        private final float areaCoverage;
        private final float entityCoverage;

        protected Used(SpellBuildCondition.Context ctx) {
            super(SurfaceSprayEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            this.effects = SurfaceSprayEffect.this.effects.stream().
                    filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).
                    collect(ImmutableList.toImmutableList());
            for (var effect : this.effects) {
                this.globalReactions.addAll(effect.getGlobalReactions());
            }

            var areaCoverage = SurfaceSprayEffect.this.areaCoverage;
            var entCoverage = SurfaceSprayEffect.this.entityCoverage;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    areaCoverage = r.areaCoverage.applyMultiple(areaCoverage, ctx.stackSize);
                    entCoverage = r.entityCoverage.applyMultiple(entCoverage, ctx.stackSize);
                }
            }
            for (var reaction : this.getGlobalReactions()) {
                if (reaction instanceof Reaction r) {
                    areaCoverage = r.areaCoverage.applyMultiple(areaCoverage, ctx.stackSize);
                    entCoverage = r.entityCoverage.applyMultiple(entCoverage, ctx.stackSize);
                }
            }
            this.areaCoverage = areaCoverage;
            this.entityCoverage = entCoverage;
        }

        protected Used(JsonObject obj) {
            super(SurfaceSprayEffect.this, obj);
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect.Used::fromJson);
            this.areaCoverage = obj.get("area_coverage").getAsFloat();
            this.entityCoverage = obj.get("entity_coverage").getAsFloat();
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("effects", JsonHelpers.toJsonList(this.effects));
            obj.addProperty("area_coverage", this.areaCoverage);
            obj.addProperty("entity_coverage", this.entityCoverage);
        }

        private ActionResult apply(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            var lastResult = ActionResult.PASS;
            for (var effect : this.effects) {
                lastResult = effect.useOnBlock(cast, world, pos, direction);
                if (lastResult.equals(ActionResult.FAIL) || lastResult.equals(ActionResult.CONSUME)) {
                    break;
                }
            }
            return lastResult;
        }

        private ActionResult apply(ServerSpellCast cast, Entity entity) {
            var lastResult = ActionResult.PASS;
            for (var effect : this.effects) {
                lastResult = effect.useOnEntity(cast, entity);
                if (lastResult.equals(ActionResult.FAIL) || lastResult.equals(ActionResult.CONSUME)) {
                    break;
                }
            }
            return lastResult;
        }

        @Override
        public @NotNull ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaCollider collider) {
            var bb = new BlockBox(
                    (int) boundingBox.minX, (int) boundingBox.minY, (int) boundingBox.minZ,
                    (int) boundingBox.maxX, (int) origin.y, (int) boundingBox.maxZ
            );

            ActionResult lastResult = ActionResult.PASS;

            if (this.areaCoverage > 0) {
                var startPos = cast.getOriginPos();
                var pos = new BlockPos.Mutable(0, 0, 0);
                for (int x = bb.getMinX(); x <= bb.getMaxX(); x++) {
                    for (int z = bb.getMinZ(); z <= bb.getMaxZ(); z++) {
                        for (int y = bb.getMaxY(); y > bb.getMinY(); y--) {
                            pos.set(x, y, z);

                            if (!collider.containsPos(pos.getX(), pos.getY(), pos.getZ())) {
                                continue;
                            }

                            if (world.isAir(pos)) continue;

                            // protect caster
                            if (Math.abs(pos.getX() - startPos.getX()) <= 1 &&
                                    Math.abs(pos.getY() - startPos.getY()) <= 2 &&
                                    Math.abs(pos.getZ() - startPos.getZ()) <= 1) {
                                continue;
                            }

                            if (RPGKitMagicMod.RANDOM.nextFloat() <= this.areaCoverage) {
                                lastResult = this.apply(cast, world, pos, Direction.UP);
                                if (lastResult.equals(ActionResult.CONSUME) || lastResult.equals(ActionResult.FAIL)) {
                                    return lastResult;
                                }
                            }

                            // affect only first non-air block ("surface" spray)
                            break;
                        }
                    }
                }
            }

            if (this.entityCoverage > 0) {
                var targetEnts = world.getOtherEntities(null, Box.from(bb), entity -> !entity.isSpectator() &&
                        collider.containsPos(entity.getX(), entity.getY(), entity.getZ()) &&
                        RPGKitMagicMod.RANDOM.nextFloat() <= this.entityCoverage);
                for (var ent : targetEnts) {
                    lastResult = this.apply(cast, ent);
                    if (lastResult.equals(ActionResult.CONSUME) || lastResult.equals(ActionResult.FAIL)) {
                        return lastResult;
                    }
                }
            }

            return lastResult;
        }
    }


    protected final float areaCoverage;
    protected final float entityCoverage;
    protected final ImmutableList<UseEffect> effects;

    protected SurfaceSprayEffect(Identifier id) {
        super(id);
        this.areaCoverage = 0.25f;
        this.entityCoverage = 1f;
        this.effects = ImmutableList.of();
    }

    protected SurfaceSprayEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("area_coverage")) {
            this.areaCoverage = obj.get("area_coverage").getAsFloat();
        } else {
            this.areaCoverage = 0.25f;
        }
        if (obj.has("entity_coverage")) {
            this.entityCoverage = obj.get("entity_coverage").getAsFloat();
        } else {
            this.entityCoverage = 1f;
        }
        if (obj.has("effects")) {
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect::fromJson);
        } else {
            this.effects = ImmutableList.of();
        }
    }

    @Override
    public AreaEffect.@NotNull Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public AreaEffect.@NotNull Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("area_coverage", this.areaCoverage);
        obj.addProperty("entity_coverage", this.entityCoverage);
        obj.add("effects", JsonHelpers.toJsonList(this.effects));
    }
}
