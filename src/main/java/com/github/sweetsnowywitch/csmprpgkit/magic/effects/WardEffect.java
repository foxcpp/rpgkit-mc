package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.components.ModComponents;
import com.github.sweetsnowywitch.csmprpgkit.components.chunk.MagicEffectsComponent;
import com.github.sweetsnowywitch.csmprpgkit.effects.ModStatusEffects;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellArea;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WardEffect extends SpellEffect {
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

    public static class Area extends SpellArea {
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
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        if (entity.getUuid().equals(cast.getCasterUuid()) && this.allowCaster) {
            return false;
        }

        if (!(entity instanceof LivingEntity le)) {
            return false;
        }

        var caster = ((ServerWorld) entity.getWorld()).getEntity(cast.getCasterUuid());

        le.addStatusEffect(
                new StatusEffectInstance(ModStatusEffects.SEALED,
                        this.duration, 0,
                        false, false),
                caster
        );
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        MagicEffectsComponent.addGlobalArea(world,
                new Area(new BlockBox(pos), this, cast, this.duration,
                        this.strength, this.allowCaster ? cast.getCasterUuid() : null));
        return false;
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        // TODO: Create force-field that cannot be passed.
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
    public void toJson(JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("strength", this.strength);
        obj.addProperty("duration", this.duration);
        obj.addProperty("allow_caster", this.allowCaster);
    }
}
