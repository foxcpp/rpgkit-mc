package com.github.sweetsnowywitch.rpgkit.magic.form;

import com.github.sweetsnowywitch.rpgkit.magic.RPGKitMagicMod;
import com.github.sweetsnowywitch.rpgkit.magic.effects.AreaEffect;
import com.github.sweetsnowywitch.rpgkit.magic.particle.GenericSpellParticleEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellElement;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellForm;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class AreaForm extends SpellForm {
    public enum Shape {
        BOX,
        CYLINDER;

        public AreaEffect.AreaCollider collider(Vec3d origin, double radius) {
            return switch (this) {
                case BOX -> AreaEffect.AreaCollider.cube(Box.of(origin, radius * 2, radius * 2, radius * 2));
                case CYLINDER -> AreaEffect.AreaCollider.cylinder(radius, radius, origin);
            };
        }
    }

    public static class Reaction extends SpellReaction {
        public final double radius;
        public final Shape shape;

        public Reaction(JsonObject obj) {
            super(Type.FORM, obj);

            if (obj.has("radius")) {
                this.radius = obj.get("radius").getAsDouble();
            } else {
                this.radius = 0;
            }

            if (obj.has("shape")) {
                this.shape = Shape.valueOf(obj.get("shape").getAsString().toUpperCase());
            } else {
                this.shape = Shape.BOX;
            }
        }

        @Override
        public boolean appliesTo(SpellForm form) {
            return form instanceof AreaForm;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("radius", this.radius);
            obj.addProperty("shape", this.shape.name().toLowerCase());
        }

        @Override
        public String toString() {
            return "AreaForm.Reaction[" +
                    "shape=" + shape.toString() + ", " +
                    "radius=" + radius +
                    ']';
        }
    }

    private final double radius;
    private final Shape shape;

    public AreaForm() {
        super(ImmutableMap.of(
                SpellElement.COST_MAGICAE, 2.75f,
                SpellElement.COST_INTERITIO, 2f
        ), ImmutableMap.of());

        this.radius = 4;
        this.shape = Shape.BOX;
    }

    @Override
    public void startCast(@NotNull ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        super.startCast(cast, world, caster);

        double radius = this.radius;
        var shape = this.shape;
        for (var reaction : cast.getSpell().getGlobalReactions()) {
            if (reaction instanceof Reaction r) {
                radius += r.radius;
                shape = r.shape;
            }
        }
        if (radius <= 2) {
            radius = 2;
        }

        var area = Box.of(caster.getPos(), radius * 2, radius * 2, radius * 2);
        var collider = shape.collider(caster.getPos(), radius);

        RPGKitMagicMod.LOGGER.debug("Spell affects area {} ({})", area, collider);

        cast.getSpell().useOnArea(cast, world, area, caster.getPos(), collider);

        var volume = (area.maxX - area.minX) * (area.maxZ - area.minZ) * (area.maxY - area.minY);
        var center = area.getCenter();
        world.spawnParticles(new GenericSpellParticleEffect(SpellElement.calculateBaseColor(cast.getFullRecipe()), 10),
                center.getX(), center.getY(), center.getZ(), (int) (volume / 40 + 1),
                area.getXLength() / 2, area.getYLength() / 2, area.getZLength() / 2, 0);
    }

    @Override
    public void endCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world) {
        super.endCast(cast, world);
    }
}
