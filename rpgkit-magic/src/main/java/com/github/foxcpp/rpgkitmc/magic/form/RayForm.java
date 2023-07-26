package com.github.foxcpp.rpgkitmc.magic.form;

import com.github.foxcpp.rpgkitmc.magic.entities.ModEntities;
import com.github.foxcpp.rpgkitmc.magic.entities.SpellRayEntity;
import com.github.foxcpp.rpgkitmc.magic.items.ModItems;
import com.github.foxcpp.rpgkitmc.magic.spell.*;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;

public class RayForm extends SpellForm implements ChanneledForm {
    public static class Reaction extends SpellReaction {
        public final boolean canHitItems;
        public final RaycastContext.FluidHandling fluidHandling;

        protected Reaction(JsonObject obj) {
            super(Type.FORM, obj);
            this.canHitItems = obj.has("can_hit_items") && obj.get("can_hit_items").getAsBoolean();
            if (obj.has("fluid_handling")) {
                this.fluidHandling = RaycastContext.FluidHandling.valueOf(obj.get("fluid_handling").getAsString().toUpperCase());
            } else {
                this.fluidHandling = RaycastContext.FluidHandling.ANY;
            }
        }

        @Override
        public boolean appliesTo(SpellForm form) {
            return form instanceof RayForm;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("can_hit_items", this.canHitItems);
            obj.addProperty("fluid_handling", this.fluidHandling.name().toLowerCase());
        }
    }

    public RayForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    @Override
    public void startCast(@NotNull ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        var ray = new SpellRayEntity(ModEntities.SPELL_RAY, world);
        cast.customData.putUuid("RayEntityUUID", ray.getUuid());
        ray.setCast(cast);

        var canHitItems = false;
        var fluidHandling = RaycastContext.FluidHandling.ANY;
        for (var reaction : cast.getSpell().getGlobalReactions()) {
            if (reaction instanceof Reaction r) {
                canHitItems = canHitItems || r.canHitItems;
                fluidHandling = r.fluidHandling;
            }
        }
        ray.setCanHitItems(canHitItems);

        ray.setAimOrigin(caster.getEyePos());
        var pos = caster.getPos();
        if (caster instanceof PlayerEntity pe) {
            pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
            pos = pos.add(0, 0.7, 0);
        }
        ray.setPosition(pos);
        ray.setYaw(caster.getHeadYaw());
        ray.setPitch(caster.getPitch());
        ray.setMaxAge(this.getMaxChannelDuration(cast.getSpell()));
        ray.setFluidHandling(fluidHandling);
        world.spawnEntity(ray);

        super.startCast(cast, world, caster);
    }

    @Override
    public void endCast(@NotNull ServerSpellCast cast, @NotNull ServerWorld world) {
        super.endCast(cast, world);

        if (cast.customData.contains("RayEntityUUID")) {
            var ent = world.getEntity(cast.customData.getUuid("RayEntityUUID"));
            if (ent != null) {
                ent.discard();
            }
        }
    }

    @Override
    public int getMaxChannelDuration(Spell cast) {
        return 5 * 20;
    }

    @Override
    public void channelTick(ServerSpellCast cast, @NotNull Entity caster) {
        if (cast.customData.containsUuid("RayEntityUUID")) {
            var ray = (SpellRayEntity) ((ServerWorld) caster.getWorld()).getEntity(cast.customData.getUuid("RayEntityUUID"));
            if (ray != null) {
                ray.setAimOrigin(caster.getEyePos());
                var pos = caster.getPos();
                if (caster instanceof PlayerEntity pe) {
                    pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
                    pos = pos.add(0, 0.7, 0);
                }
                ray.setPosition(pos);
                ray.setYaw(caster.getHeadYaw());
                ray.setPitch(caster.getPitch());
                cast.updateOrigin(pos);
                cast.updateOriginRotation(caster.getHeadYaw(), caster.getPitch());
                ray.setMaxAge(ray.age + 5);
            }
        }
    }
}
