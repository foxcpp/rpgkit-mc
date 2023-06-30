package com.github.sweetsnowywitch.rpgkit.magic.effects.area;

import com.github.sweetsnowywitch.rpgkit.JsonHelpers;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
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

public class PersistentAreaEffect extends AreaEffect {
    public static class Reaction extends SpellReaction {
        private final IntModifier duration;
        private final IntModifier effectInterval;

        protected Reaction(JsonObject obj) {
            super(obj);

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
    }

    protected final int durationTicks;
    protected final int effectIntervalTicks;
    protected final ImmutableList<UseEffect> effects;

    protected PersistentAreaEffect(Identifier id) {
        super(id);
        this.durationTicks = 60;
        this.effectIntervalTicks = 20;
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
        if (obj.has("effects")) {
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect::fromJson);
        } else {
            this.effects = ImmutableList.of();
        }
    }

    public class Used extends AreaEffect.Used {
        protected final int durationTicks;
        protected final int effectIntervalTicks;
        protected final ImmutableList<UseEffect.Used> effects;

        protected Used(SpellBuildCondition.Context ctx) {
            super(PersistentAreaEffect.this, new ArrayList<>(), ctx);
            var duration = PersistentAreaEffect.this.durationTicks;
            var effectInterval = PersistentAreaEffect.this.effectIntervalTicks;
            for (var reaction : this.appliedReactions) {
                if (reaction instanceof Reaction r) {
                    duration = r.duration.apply(duration);
                    effectInterval = r.effectInterval.apply(effectInterval);
                }
            }
            this.durationTicks = duration;
            this.effectIntervalTicks = effectInterval;
            this.effects = PersistentAreaEffect.this.effects.stream().map(eff -> eff.use(ctx)).collect(ImmutableList.toImmutableList());
        }

        protected Used(JsonObject obj) {
            super(PersistentAreaEffect.this, obj);
            this.durationTicks = obj.get("duration").getAsInt();
            this.effectIntervalTicks = obj.get("interval").getAsInt();
            this.effects = JsonHelpers.fromJsonList(obj.getAsJsonArray("effects"), UseEffect.Used::fromJson);
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("duration", this.durationTicks);
            obj.addProperty("interval", this.effectIntervalTicks);
            obj.add("effects", JsonHelpers.toJsonList(this.effects));
        }

        @Override
        public ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaCollider collider) {
            var cast2 = cast.withSpell(new Spell(this.effects, cast.getSpell().getFormReactions(), cast.getSpell().getUseForm()));
            return cast2.getSpell().useOnArea(cast, world, boundingBox, origin, collider);
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
