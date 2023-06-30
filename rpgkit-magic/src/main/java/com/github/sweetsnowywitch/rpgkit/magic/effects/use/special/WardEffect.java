package com.github.sweetsnowywitch.rpgkit.magic.effects.use.special;

import com.github.sweetsnowywitch.rpgkit.magic.MagicArea;
import com.github.sweetsnowywitch.rpgkit.magic.components.ModComponents;
import com.github.sweetsnowywitch.rpgkit.magic.components.chunk.MagicEffectsComponent;
import com.github.sweetsnowywitch.rpgkit.magic.effects.ModStatusEffects;
import com.github.sweetsnowywitch.rpgkit.magic.effects.SpellEffect;
import com.github.sweetsnowywitch.rpgkit.magic.effects.use.SimpleUseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class WardEffect extends SimpleUseEffect {
    private static class Listener implements AttackBlockCallback, UseBlockCallback {
        @Override
        public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
            if (WardEffect.isBlockProtected(world, pos, player)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        @Override
        public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
            if (WardEffect.isBlockProtected(world, hitResult.getBlockPos(), player)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
    }

    public static void registerListener() {
        var l = new Listener();
        UseBlockCallback.EVENT.register(l);
        AttackBlockCallback.EVENT.register(l);
    }

    public static class Area extends MagicArea {
        private final int strength;
        private final @Nullable UUID holderId;

        public Area(BlockBox box, SpellEffect effect, SpellCast cast, int maxAge, int strength, @Nullable UUID holderId) {
            super(box, effect, cast, maxAge);
            this.strength = strength;
            this.holderId = holderId;
        }

        public Area(Identifier effectID, NbtCompound tag) {
            super(effectID, tag);
            this.strength = tag.getInt("Strength");
            if (tag.containsUuid("HolderId")) {
                this.holderId = tag.getUuid("HolderId");
            } else {
                this.holderId = null;
            }
        }

        @Override
        public void writeToNbt(NbtCompound tag) {
            super.writeToNbt(tag);
            tag.putInt("Strength", this.strength);
            if (this.holderId != null) {
                tag.putUuid("HolderId", this.holderId);
            }
        }
    }

    private final int strength;
    private final int duration;
    private final boolean allowCaster;

    public WardEffect(Identifier id) {
        super(id);
        this.strength = 1;
        this.duration = 30 * 20;
        this.allowCaster = false;
    }

    public WardEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("strength")) {
            this.strength = obj.get("strength").getAsInt();
        } else {
            this.strength = 1;
        }
        if (obj.has("duration")) {
            this.duration = obj.get("duration").getAsInt();
        } else {
            this.duration = 30 * 20;
        }
        if (obj.has("allow_caster")) {
            this.allowCaster = obj.get("allow_caster").getAsBoolean();
        } else {
            this.allowCaster = false;
        }
    }

    @Override
    protected ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions) {
        if (entity.getUuid().equals(cast.getCasterUuid()) && this.allowCaster) {
            return ActionResult.PASS;
        }

        if (!(entity instanceof LivingEntity le)) {
            return ActionResult.PASS;
        }

        var caster = ((ServerWorld) entity.getWorld()).getEntity(cast.getCasterUuid());

        le.addStatusEffect(
                new StatusEffectInstance(ModStatusEffects.SEALED,
                        this.duration, 0,
                        false, false),
                caster
        );
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        MagicEffectsComponent.addGlobalArea(world,
                new Area(new BlockBox(pos), this, cast, this.duration,
                        this.strength, this.allowCaster ? cast.getCasterUuid() : null));
        return ActionResult.SUCCESS;
    }

    public static boolean isBlockProtected(World world, BlockPos pos) {
        var comp = world.getChunk(pos).getComponent(ModComponents.CHUNK_MAGIC_EFFECTS);
        var areas = comp.getAreas(pos, WardEffect.Area.class);
        return areas.size() == 0;
    }

    public static boolean isBlockProtected(World world, BlockPos pos, PlayerEntity player) {
        var comp = world.getChunk(pos).getComponent(ModComponents.CHUNK_MAGIC_EFFECTS);
        var areas = comp.getAreas(pos, WardEffect.Area.class);

        for (var area : areas) {
            if (area.holderId == null || !area.holderId.equals(player.getUuid())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("strength", this.strength);
        obj.addProperty("duration", this.duration);
        obj.addProperty("allow_caster", this.allowCaster);
    }
}
