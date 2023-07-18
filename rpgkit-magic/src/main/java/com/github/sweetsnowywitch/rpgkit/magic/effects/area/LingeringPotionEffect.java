package com.github.sweetsnowywitch.rpgkit.magic.effects.area;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.SpellEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.use.PotionEffect;
import com.github.sweetsnowywitch.rpgkit.magic.json.IntModifier;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class LingeringPotionEffect extends AreaEffect {
    public static class Reaction extends SpellReaction {
        private final @Nullable StatusEffect effect;
        private final IntModifier amplifier;
        private final IntModifier durationTicks;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);

            if (obj.has("id")) {
                var effectId = new Identifier(obj.get("id").getAsString());
                var effect = Registry.STATUS_EFFECT.get(effectId);
                RPGKitMagicMod.LOGGER.debug("PotionEffect.Reaction populated with potion effect {}", effectId);
                if (effect == null) {
                    throw new IllegalStateException("unknown potion effect");
                }
                this.effect = effect;
            } else {
                this.effect = null;
            }

            if (obj.has("amplifier")) {
                this.amplifier = new IntModifier(obj.get("amplifier"));
            } else {
                this.amplifier = IntModifier.NOOP;
            }
            if (obj.has("duration")) {
                this.durationTicks = new IntModifier(obj.get("duration"));
            } else {
                this.durationTicks = IntModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof PotionEffect;
        }

        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);

            if (this.effect != null) {
                var id = Registry.STATUS_EFFECT.getId(this.effect);
                if (id == null) {
                    throw new IllegalStateException("potion effect with unregistered effect");
                }
                obj.addProperty("id", id.toString());
            }

            obj.add("amplifier", this.amplifier.toJson());
            obj.add("duration", this.durationTicks.toJson());
        }
    }

    public class Used extends AreaEffect.Used {
        private final int amplifier;
        private final int duration;

        protected Used(SpellBuildCondition.Context ctx) {
            super(LingeringPotionEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
            var amplifier = LingeringPotionEffect.this.baseAmplifier;
            var duration = LingeringPotionEffect.this.baseDuration;

            for (var reaction : reactions) {
                if (reaction instanceof Reaction r && (r.effect == null || r.effect.equals(LingeringPotionEffect.this.statusEffect))) {
                    amplifier = r.amplifier.applyMultiple(amplifier, ctx.stackSize);
                    duration = r.durationTicks.applyMultiple(duration, ctx.stackSize);
                }
            }
            if (amplifier <= 0) {
                amplifier = 0;
            }
            if (duration <= 2) {
                duration = 2;
            }

            this.amplifier = amplifier;
            this.duration = duration;
        }

        protected Used(JsonObject obj) {
            super(LingeringPotionEffect.this, obj);
            this.amplifier = obj.get("amplifier").getAsInt();
            this.duration = obj.get("duration").getAsInt();
        }

        @Override
        public @NotNull ActionResult useOnArea(ServerSpellCast cast, ServerWorld world, Box boundingBox, Vec3d origin, AreaCollider collider) {
            if (LingeringPotionEffect.this.statusEffect == null) {
                RPGKitMagicMod.LOGGER.warn("Tried to apply an empty LingeringPotionEffect");
                return ActionResult.PASS;
            }
            var areaEffectCloudEntity = new AreaEffectCloudEntity(world, origin.x, origin.y, origin.z);
            var entity = cast.getCaster(world);
            if (entity instanceof LivingEntity) {
                areaEffectCloudEntity.setOwner((LivingEntity) entity);
            }
            areaEffectCloudEntity.setRadius((float) (boundingBox.getXLength() / 2));
            areaEffectCloudEntity.setRadiusOnUse(-0.5f);
            areaEffectCloudEntity.setWaitTime(10);
            areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
            areaEffectCloudEntity.addEffect(new StatusEffectInstance(LingeringPotionEffect.this.statusEffect,
                    this.duration, this.amplifier, false, LingeringPotionEffect.this.showParticles, LingeringPotionEffect.this.showIcon));
            world.spawnEntity(areaEffectCloudEntity);
            return ActionResult.SUCCESS;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("amplifier", this.amplifier);
            obj.addProperty("duration", this.duration);
        }
    }

    public static final int DEFAULT_DURATION = 20 * 10;
    public static final int DEFAULT_AMPLIFIER = 1;

    private final StatusEffect statusEffect;
    private final int baseDuration;
    private final int baseAmplifier;
    private final boolean showIcon;
    private final boolean showParticles;


    protected LingeringPotionEffect(Identifier id) {
        super(id);
        this.statusEffect = null;
        this.baseAmplifier = DEFAULT_AMPLIFIER;
        this.baseDuration = DEFAULT_DURATION;
        this.showIcon = false;
        this.showParticles = false;
    }

    protected LingeringPotionEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("id")) {
            var effectId = new Identifier(obj.get("id").getAsString());
            var effect = Registry.STATUS_EFFECT.get(effectId);
            RPGKitMagicMod.LOGGER.debug("PotionEffect populated with potion effect {}", effectId);
            if (effect == null) {
                throw new IllegalStateException("unknown potion effect");
            }
            this.statusEffect = effect;
        } else {
            this.statusEffect = null;
        }
        if (obj.has("amplifier")) {
            this.baseAmplifier = obj.get("amplifier").getAsInt();
        } else {
            this.baseAmplifier = DEFAULT_AMPLIFIER;
        }

        if (obj.has("duration")) {
            this.baseDuration = obj.get("duration").getAsInt();
        } else {
            this.baseDuration = DEFAULT_DURATION;
        }

        if (obj.has("show_icon")) {
            this.showIcon = obj.get("show_icon").getAsBoolean();
        } else {
            this.showIcon = false;
        }

        if (obj.has("show_particles")) {
            this.showParticles = obj.get("show_particles").getAsBoolean();
        } else {
            this.showParticles = false;
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
        if (this.statusEffect != null) {
            var id = Registry.STATUS_EFFECT.getId(this.statusEffect);
            if (id == null) {
                throw new IllegalStateException("potion effect with unregistered effect");
            }
            obj.addProperty("id", id.toString());
        }
        obj.addProperty("amplifier", this.baseAmplifier);
        obj.addProperty("duration", this.baseDuration);
        obj.addProperty("show_icon", this.showIcon);
        obj.addProperty("show_particles", this.showParticles);
    }
}
