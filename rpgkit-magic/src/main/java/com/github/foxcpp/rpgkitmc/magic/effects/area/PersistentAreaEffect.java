package com.github.foxcpp.rpgkitmc.magic.effects.area;

import com.github.foxcpp.rpgkitmc.JsonHelpers;
import com.github.foxcpp.rpgkitmc.magic.effects.AreaEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.entities.ModEntities;
import com.github.foxcpp.rpgkitmc.magic.entities.PersistentMagicEntity;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.Spell;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.github.foxcpp.rpgkitmc.magic.EffectVector;
import com.github.foxcpp.rpgkitmc.magic.json.IntModifier;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PersistentAreaEffect extends AreaEffect {
    public static class Reaction extends SpellReaction {
        private final IntModifier duration;
        private final IntModifier effectInterval;

        protected Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);

            if (obj.has("duration")) {
                this.duration = new IntModifier(obj.get("duration"));
            } else {
                this.duration = IntModifier.NOOP;
            }

            if (obj.has("interval")) {
                this.effectInterval = new IntModifier(obj.get("interval"));
            } else {
                this.effectInterval = IntModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof PersistentAreaEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("duration", this.duration.toJson());
            obj.add("interval", this.effectInterval.toJson());
        }
    }

    public class Used extends AreaEffect.Used {
        protected final int durationTicks;
        protected final int effectIntervalTicks;
        protected final ImmutableList<AreaEffect.Used> effects;

        protected Used(SpellBuildCondition.Context ctx) {
            super(PersistentAreaEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            var duration = PersistentAreaEffect.this.durationTicks;
            var effectInterval = PersistentAreaEffect.this.effectIntervalTicks;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    duration = r.duration.applyMultiple(duration, ctx.stackSize);
                    effectInterval = r.effectInterval.applyMultiple(effectInterval, ctx.stackSize);
                }
            }
            this.durationTicks = duration;
            this.effectIntervalTicks = effectInterval;
            this.effects = PersistentAreaEffect.this.effects.stream().
                    filter(eff -> eff.shouldAdd(ctx)).
                    map(eff -> eff.use(ctx)).
                    collect(ImmutableList.toImmutableList());
            for (var effect : this.effects) {
                this.globalReactions.addAll(effect.getGlobalReactions());
            }
        }

        protected Used(JsonObject obj) {
            super(PersistentAreaEffect.this, obj);
            this.durationTicks = obj.get("duration").getAsInt();
            this.effectIntervalTicks = obj.get("interval").getAsInt();
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), AreaEffect.Used::fromJson);
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("duration", this.durationTicks);
            obj.addProperty("interval", this.effectIntervalTicks);
            obj.add("effects", JsonHelpers.toJsonList(this.effects));
        }

        @Override
        public @NotNull ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaCollider collider) {
            if (this.effects.size() == 0) {
                return ActionResult.PASS;
            }

            var subCast = cast.withSpell(new Spell(
                    ImmutableList.of(), this.effects, ImmutableList.of(),
                    cast.getSpell().getGlobalReactions(), cast.getSpell().getUseForm()));

            var ent = new PersistentMagicEntity(ModEntities.PERSISTENT_MAGIC, world, boundingBox, this.durationTicks, this.effectIntervalTicks);
            ent.setCast(subCast);
            if (PersistentAreaEffect.this.particleVector != null) {
                ent.setParticleVector(PersistentAreaEffect.this.particleVector);
            }

            world.spawnEntity(ent);
            return ActionResult.SUCCESS;
        }
    }

    protected final int durationTicks;
    protected final int effectIntervalTicks;
    protected final @Nullable EffectVector particleVector;
    protected final ImmutableList<AreaEffect> effects;

    protected PersistentAreaEffect(Identifier id) {
        super(id);
        this.durationTicks = 60;
        this.effectIntervalTicks = 20;
        this.particleVector = null;
        this.effects = ImmutableList.of();
    }

    protected PersistentAreaEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("duration")) {
            this.durationTicks = obj.get("duration").getAsInt();
        } else {
            this.durationTicks = 60;
        }
        if (obj.has("interval")) {
            this.effectIntervalTicks = obj.get("interval").getAsInt();
        } else {
            this.effectIntervalTicks = 20;
        }
        if (obj.has("particle_vector")) {
            this.particleVector = EffectVector.fromJson(obj.get("particle_vector"));
        } else {
            this.particleVector = null;
        }
        if (obj.has("area_effects")) {
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("area_effects"), AreaEffect::fromJson);
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
        obj.addProperty("interval", this.effectIntervalTicks);
        obj.add("particle_vector", this.particleVector.toJson());
        obj.add("area_effects", JsonHelpers.toJsonList(this.effects));
    }
}
