package com.github.sweetsnowywitch.rpgkit.magic.effects.area;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SurfaceSprayEffect extends AreaEffect {
    public static class Reaction extends SpellReaction {
        protected final float areaCoverage;

        protected Reaction(JsonObject obj) {
            super(obj);
            if (obj.has("area_coverage")) {
                this.areaCoverage = obj.get("area_coverage").getAsFloat();
            } else {
                this.areaCoverage = 0.25f;
            }
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("area_coverage", this.areaCoverage);
        }
    }

    protected final float areaCoverage;
    protected final ImmutableList<UseEffect> effects;

    protected SurfaceSprayEffect(Identifier id) {
        super(id);
        this.areaCoverage = 0.25f;
        this.effects = ImmutableList.of();
    }

    protected SurfaceSprayEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("area_coverage")) {
            this.areaCoverage = obj.get("area_coverage").getAsFloat();
        } else {
            this.areaCoverage = 0.25f;
        }
        if (obj.has("effects")) {
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect::fromJson);
        } else {
            this.effects = ImmutableList.of();
        }
    }

    public class Used extends AreaEffect.Used {
        private final ImmutableList<UseEffect.Used> effects;

        protected Used(SpellBuildCondition.Context ctx) {
            super(SurfaceSprayEffect.this, new ArrayList<>(), ctx);
            this.effects = SurfaceSprayEffect.this.effects.stream().map(eff -> eff.use(ctx)).collect(ImmutableList.toImmutableList());
        }

        protected Used(JsonObject obj) {
            super(SurfaceSprayEffect.this, obj);
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect.Used::fromJson);
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("effects", JsonHelpers.toJsonList(this.effects));
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

        @Override
        public ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaCollider collider) {
            var bb = new BlockBox(
                    (int) boundingBox.minX, (int) boundingBox.minY, (int) boundingBox.minZ,
                    (int) boundingBox.maxX, (int) origin.y, (int) boundingBox.maxZ
            );

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

                        if (RPGKitMagicMod.RANDOM.nextFloat() <= SurfaceSprayEffect.this.areaCoverage) {
                            this.apply(cast, world, pos, Direction.UP);
                        }

                        // affect only first non-air block ("surface" spray)
                        break;
                    }
                }
            }

            return null;
        }
    }

    @Override
    public AreaEffect.Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public AreaEffect.Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }
}
