package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.events.MagicBlockEvents;
import com.github.foxcpp.rpgkitmc.magic.MagicStrengthReaction;
import com.github.foxcpp.rpgkitmc.magic.ProtectionBreakingEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.json.BlockStatePredicate;
import com.github.foxcpp.rpgkitmc.magic.json.FloatModifier;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class BreakBlockEffect extends UseEffect {
    private final boolean drop;
    private final float magicStrength;

    private final @Nullable BlockStatePredicate filter;

    public static class Reaction extends MagicStrengthReaction {
        protected Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof BreakBlockEffect;
        }
    }

    public BreakBlockEffect(Identifier id) {
        super(id);
        this.drop = false;
        this.magicStrength = 0.5f;
        this.filter = null;
    }

    public BreakBlockEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        this.drop = obj.has("drop") && obj.get("drop").getAsBoolean();
        if (obj.has("magic_strength")) {
            this.magicStrength = obj.get("magic_strength").getAsFloat();
        } else {
            this.magicStrength = 0.5f;
        }
        if (obj.has("filter")) {
            this.filter = new BlockStatePredicate(obj.get("filter"));
        } else {
            this.filter = null;
        }
    }

    public class Used extends UseEffect.Used implements ProtectionBreakingEffect {
        private final float magicStrength;

        protected Used(SpellBuildCondition.Context ctx) {
            super(BreakBlockEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);

            var magStrength = BreakBlockEffect.this.magicStrength;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    magStrength = r.magicStrength.applyMultiple(magStrength, ctx.stackSize);
                }
            }
            this.magicStrength = magStrength;
        }

        protected Used(JsonObject obj) {
            super(BreakBlockEffect.this, obj);
            this.magicStrength = obj.get("magic_strength").getAsFloat();
        }

        @Override
        public @NotNull ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            if (BreakBlockEffect.this.filter != null && !BreakBlockEffect.this.filter.test(world.getBlockState(pos))) {
                return ActionResult.PASS;
            }

            var eventResult = MagicBlockEvents.DAMAGE.invoker().onBlockMagicDamaged(cast, this, world, pos);
            if (eventResult.equals(ActionResult.FAIL) || eventResult.equals(ActionResult.CONSUME) || eventResult.equals(ActionResult.CONSUME_PARTIAL)) {
                return eventResult;
            }

            if (!world.isInBuildLimit(pos)) {
                return ActionResult.PASS;
            }

            var playerCaster = cast.getPlayerCaster(world);
            if (playerCaster != null && !world.canPlayerModifyAt(playerCaster, pos)) {
                return ActionResult.PASS;
            }

            if (world.breakBlock(pos, BreakBlockEffect.this.drop, cast.getCaster(world))) {
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }

        @Override
        public @NotNull ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            var world = (ServerWorld) entity.getWorld();

            if (BreakBlockEffect.this.filter != null && !BreakBlockEffect.this.filter.test(world.getBlockState(entity.getBlockPos()))) {
                return ActionResult.PASS;
            }

            var eventResult = MagicBlockEvents.DAMAGE.invoker().onBlockMagicDamaged(cast, this, world, entity.getBlockPos());
            if (eventResult.equals(ActionResult.FAIL) || eventResult.equals(ActionResult.CONSUME) || eventResult.equals(ActionResult.CONSUME_PARTIAL)) {
                return eventResult;
            }

            if (!world.isInBuildLimit(entity.getBlockPos())) {
                return ActionResult.PASS;
            }

            var playerCaster = cast.getPlayerCaster(world);
            if (playerCaster != null && !world.canPlayerModifyAt(playerCaster, entity.getBlockPos())) {
                return ActionResult.PASS;
            }

            if (world.breakBlock(entity.getBlockPos(), BreakBlockEffect.this.drop, cast.getCaster(world))) {
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("magic_strength", this.magicStrength);
        }

        @Override
        public @NotNull FloatModifier calculateEffectReduction(ServerSpellCast cast, float protectionStrength) {
            if (protectionStrength > this.magicStrength) {
                return FloatModifier.ZEROED;
            }
            return FloatModifier.NOOP;
        }

        @Override
        public boolean willDissolveProtection(ServerSpellCast cast, float protectionStrength) {
            return this.magicStrength >= protectionStrength * 2;
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
        obj.addProperty("drop", this.drop);
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
    }
}
