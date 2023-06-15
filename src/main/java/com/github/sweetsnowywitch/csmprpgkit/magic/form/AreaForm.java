package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.entities.MagicAreaEntity;
import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.PersistentMagicEntity;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellElement;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.github.sweetsnowywitch.csmprpgkit.particle.GenericSpellParticleEffect;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;

public class AreaForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        public final double radius;
        public final int persistenceDuration;
        public final int effectActivations;

        public Reaction(Identifier id) {
            super(id);
            this.radius = 0;
            this.persistenceDuration = 1;
            this.effectActivations = 0;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id, obj);

            if (obj.has("radius")) {
                this.radius = obj.get("radius").getAsDouble();
            } else {
                this.radius = 0;
            }

            if (obj.has("persistence_duration_mul")) {
                this.persistenceDuration = obj.get("persistence_duration_mul").getAsInt();
            } else {
                this.persistenceDuration = 1;
            }

            if (obj.has("persistence_activations")) {
                this.effectActivations = obj.get("persistence_activations").getAsInt();
            } else {
                this.effectActivations = 0;
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
            obj.addProperty("persistence_duration_mul", this.persistenceDuration);
            obj.addProperty("persistence_activations", this.effectActivations);
        }

        @Override
        public String toString() {
            return "AreaForm.Reaction[" +
                    "persistenceDuration=" + persistenceDuration + ", " +
                    "radius=" + radius +
                    ']';
        }
    }

    public AreaForm() {
        super(ImmutableMap.of(
                SpellElement.COST_MAGICAE, 2.75f,
                SpellElement.COST_INTERITIO, 2f
        ), ImmutableMap.of());
    }

    @Override
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        super.startCast(cast, world, caster);

        double radius = 2;
        int persistenceDuration = 1;
        int effectActivations = 1;
        for (var reaction : cast.getReactions()) {
            if (reaction instanceof Reaction r) {
                radius += r.radius;
                persistenceDuration *= r.persistenceDuration;
                effectActivations += r.effectActivations;
            }
        }
        if (radius <= 2) {
            radius = 2;
        }

        var area = Box.of(caster.getPos(), radius * 2, radius * 2, radius * 2);

        RPGKitMod.LOGGER.debug("Spell affects area {}", area);

        for (var ent : world.getOtherEntities(caster, area)) {
            if (ent instanceof MagicAreaEntity) { // TODO: Magic field interactions.
                continue;
            }
            cast.getSpell().onSingleEntityHit(cast, ent);
        }

        cast.getSpell().onAreaHit(cast, world, area);

        var volume = (area.maxX - area.minX) * (area.maxZ - area.minZ) * (area.maxY - area.minY);
        var center = area.getCenter();
        world.spawnParticles(new GenericSpellParticleEffect(SpellElement.calculateBaseColor(cast.getFullRecipe()), 10),
                center.getX(), center.getY(), center.getZ(), (int) (volume / 40 + 1),
                area.getXLength() / 2, area.getYLength() / 2, area.getZLength() / 2, 0);

        if (persistenceDuration > 1) {
            var persistent = new PersistentMagicEntity(ModEntities.PERSISTENT_MAGIC, world, area,
                    persistenceDuration, persistenceDuration / effectActivations);
            persistent.setCast(cast);
            cast.customData.putUuid("PersistentAreaEntityUUID", persistent.getUuid());
            world.spawnEntity(persistent);
        }
    }

    @Override
    public void endCast(ServerSpellCast cast, ServerWorld world) {
        super.endCast(cast, world);
    }
}
