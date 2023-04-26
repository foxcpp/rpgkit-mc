package com.github.sweetsnowywitch.csmprpgkit.magic.form;

import com.github.sweetsnowywitch.csmprpgkit.entities.ModEntities;
import com.github.sweetsnowywitch.csmprpgkit.entities.SpellRayEntity;
import com.github.sweetsnowywitch.csmprpgkit.items.ModItems;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellForm;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RayForm extends SpellForm {
    public static class Reaction extends SpellReaction {
        private final int addBounces;

        public Reaction(Identifier id) {
            super(id);
            this.addBounces = 0;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id, obj);
            if (obj.has("bounces")) {
                this.addBounces = obj.get("bounces").getAsInt();
            } else {
                this.addBounces = 0;
            }
        }

        @Override
        public boolean appliesTo(SpellForm form) {
            return form instanceof RayForm;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            obj.addProperty("bounces", this.addBounces);
        }

        @Override
        public String toString() {
            return "RayForm.Reaction[" +
                    "addBounces=" + addBounces +
                    ']';
        }
    }

    public RayForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var ray = new SpellRayEntity(ModEntities.SPELL_RAY, world);
        ray.setCast(cast);
        ray.setGrowthSpeed(2f);

        var pos = caster.getPos();
        if (caster instanceof PlayerEntity pe) {
            pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
            pos = pos.add(0, 0.7, 0);
        }
        ray.setPosition(pos);
        ray.setYaw(caster.getHeadYaw());
        ray.setPitch(caster.getPitch());

        var bounces = 0;
        for (var reaction : cast.getReactions()) {
            if (reaction instanceof Reaction r) {
                bounces += r.addBounces;
            }
        }
        ray.setTotalBounces(bounces);

        world.spawnEntity(ray);

        super.startCast(cast, world, caster);
    }
}
