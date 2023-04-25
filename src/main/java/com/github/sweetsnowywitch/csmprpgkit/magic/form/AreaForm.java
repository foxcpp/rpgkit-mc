package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.MagicAreaEntity;
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
import org.jetbrains.annotations.Nullable;

public class AreaForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        public final double radius;

        public Reaction(Identifier id) {
            this(id,0);
        }

        public Reaction(Identifier id, double radius) {
            super(id);
            this.radius = radius;
        }

        @Override
        public SpellReaction withParametersFromJSON(JsonObject jsonObject) {
            var radius = this.radius;
            if (jsonObject.has("radius")) {
                radius = jsonObject.get("radius").getAsDouble();
            }
            return new Reaction(this.id, radius);
        }

        @Override
        public JsonObject parametersToJSON() {
            var obj = new JsonObject();
            obj.addProperty("radius", this.radius);
            return obj;
        }
    }

    public AreaForm() {
        super(ImmutableMap.of(
                SpellElement.COST_MAGICAE, 2.75f,
                SpellElement.COST_INTERITIO, 2f
        ), ImmutableMap.of());
    }

    @Override
    public @Nullable SpellReaction reactionType(Identifier id) {
        return new Reaction(id);
    }

    @Override
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        super.startCast(cast, world, caster);

        double radius = 2;
        for (var reaction : cast.getReactions()) {
            if (reaction instanceof Reaction r) {
                radius += r.radius;
            }
        }

        var area = Box.of(caster.getPos(), radius*2, radius*2, radius*2);

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
                center.getX(), center.getY(), center.getZ(), (int)(volume / 40 + 1),
                area.getXLength()/2, area.getYLength()/2, area.getZLength()/2, 0);
    }

    @Override
    public void endCast(ServerSpellCast cast, ServerWorld world) {
        super.endCast(cast, world);
    }
}
