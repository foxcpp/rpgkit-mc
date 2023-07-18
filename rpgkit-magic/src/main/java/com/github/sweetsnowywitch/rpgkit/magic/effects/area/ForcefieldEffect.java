package com.github.sweetsnowywitch.rpgkit.magic.effects.area;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.SpellEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.entities.ForcefieldEntity;
import com.github.sweetsnowywitch.rpgkit.magic.entities.ModEntities;
import com.github.sweetsnowywitch.rpgkit.magic.json.IntModifier;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.Spell;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ForcefieldEffect extends AreaEffect {
    public static class Reaction extends SpellReaction {
        private final IntModifier duration;

        protected Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);

            if (obj.has("duration")) {
                this.duration = new IntModifier(obj.get("duration"));
            } else {
                this.duration = IntModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof ForcefieldEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("duration", this.duration.toJson());
        }
    }

    public class Used extends AreaEffect.Used {
        protected final int durationTicks;
        protected final ImmutableList<UseEffect.Used> effects;

        protected Used(SpellBuildCondition.Context ctx) {
            super(ForcefieldEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            var duration = ForcefieldEffect.this.durationTicks;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof ForcefieldEffect.Reaction r) {
                    duration = r.duration.applyMultiple(duration, ctx.stackSize);
                }
            }
            this.durationTicks = duration;
            this.effects = ForcefieldEffect.this.effects.stream().
                    filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).
                    collect(ImmutableList.toImmutableList());
            for (var effect : this.effects) {
                this.globalReactions.addAll(effect.getGlobalReactions());
            }
        }

        protected Used(JsonObject obj) {
            super(ForcefieldEffect.this, obj);
            this.durationTicks = obj.get("duration").getAsInt();
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect.Used::fromJson);
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("duration", this.durationTicks);
            obj.add("effects", JsonHelpers.toJsonList(this.effects));
        }

        @Override
        public @NotNull ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaCollider collider) {
            if (this.effects.size() == 0) {
                return ActionResult.PASS;
            }

            var subCast = cast.withSpell(new Spell(
                    this.effects,
                    cast.getSpell().getGlobalReactions(), cast.getSpell().getUseForm()));

            var ent = new ForcefieldEntity(ModEntities.FORCEFIELD, world, boundingBox, this.durationTicks);
            ent.setCast(subCast);

            world.spawnEntity(ent);
            return ActionResult.SUCCESS;
        }
    }

    protected final int durationTicks;
    protected final ImmutableList<UseEffect> effects;

    protected ForcefieldEffect(Identifier id) {
        super(id);
        this.durationTicks = 40;
        this.effects = ImmutableList.of();
    }

    protected ForcefieldEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("duration")) {
            this.durationTicks = obj.get("duration").getAsInt();
        } else {
            this.durationTicks = 40;
        }
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
        obj.addProperty("duration", this.durationTicks);
        obj.add("effects", JsonHelpers.toJsonList(this.effects));
    }
}
