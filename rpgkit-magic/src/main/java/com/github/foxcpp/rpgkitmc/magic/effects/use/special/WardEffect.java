package com.github.foxcpp.rpgkitmc.magic.effects.use.special;

import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import com.github.foxcpp.rpgkitmc.magic.components.chunk.MagicEffectsComponent;
import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.use.SimpleUseEffect;
import com.github.foxcpp.rpgkitmc.magic.events.MagicBlockEvents;
import com.github.foxcpp.rpgkitmc.magic.items.SpellItem;
import com.github.foxcpp.rpgkitmc.magic.json.FloatModifier;
import com.github.foxcpp.rpgkitmc.magic.json.IntModifier;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.github.foxcpp.rpgkitmc.magic.MagicArea;
import com.github.foxcpp.rpgkitmc.magic.ProtectionBreakingEffect;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
    private static class Listener implements AttackBlockCallback, UseBlockCallback, MagicBlockEvents.Damage, MagicBlockEvents.Interact {
        @Override
        public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
            if (WardEffect.isBlockProtected(world, pos, player)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        @Override
        public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
            // Allow to apply spells on warded blocks - they might even break the ward.
            if (player.getStackInHand(hand).getItem() instanceof SpellItem) {
                return ActionResult.PASS;
            }
            if (WardEffect.isBlockProtected(world, hitResult.getBlockPos(), player)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        @Override
        public @NotNull ActionResult onBlockMagicDamaged(ServerSpellCast cast, UseEffect.Used effect, ServerWorld world, BlockPos pos) {
            if (!WardEffect.isBlockProtected(world, pos, cast.getPlayerCaster(world))) {
                return ActionResult.PASS;
            }

            if (effect.effect instanceof ProtectionBreakingEffect pbue) {
                return this.checkProtectionBreakingEffect(pbue, cast, world, pos, false);
            }
            if (effect instanceof ProtectionBreakingEffect pbue) {
                return this.checkProtectionBreakingEffect(pbue, cast, world, pos, false);
            }

            return ActionResult.CONSUME;
        }

        @Override
        public @NotNull ActionResult onBlockMagicInteract(ServerSpellCast cast, UseEffect.Used effect, ServerWorld world, BlockPos pos) {
            if (!WardEffect.isBlockProtected(world, pos, cast.getPlayerCaster(world))) {
                return ActionResult.PASS;
            }

            if (effect.effect instanceof ProtectionBreakingEffect pbue) {
                return this.checkProtectionBreakingEffect(pbue, cast, world, pos, true);
            }
            if (effect instanceof ProtectionBreakingEffect pbue) {
                return this.checkProtectionBreakingEffect(pbue, cast, world, pos, true);
            }

            return ActionResult.CONSUME;
        }

        private ActionResult checkProtectionBreakingEffect(ProtectionBreakingEffect pbue, ServerSpellCast cast, ServerWorld world, BlockPos pos, boolean allowBypass) {
            var comp = world.getChunk(pos).getComponent(ModComponents.CHUNK_MAGIC_EFFECTS);
            var areas = comp.getAreas(pos, Area.class);

            var strength = 0f;
            for (var area : areas) {
                if (area.getStrength() > strength) {
                    strength = area.getStrength();
                }
            }

            if (pbue.willDissolveProtection(cast, strength)) {
                comp.removeAreas(areas);
                return ActionResult.PASS;
            }
            if (!allowBypass) {
                return ActionResult.CONSUME;
            }

            if (pbue.calculateEffectReduction(cast, strength).apply(1) == 0) {
                return ActionResult.CONSUME;
            }
            return ActionResult.PASS;
        }
    }

    public static void registerListener() {
        var l = new Listener();
        UseBlockCallback.EVENT.register(l);
        AttackBlockCallback.EVENT.register(l);
        MagicBlockEvents.DAMAGE.register(l);
        MagicBlockEvents.INTERACT.register(l);
    }

    public static class Area extends MagicArea {
        private final float strength;
        private final @Nullable UUID holderId;

        public Area(BlockBox box, SpellEffect effect, SpellCast cast, int maxAge, float strength, @Nullable UUID holderId) {
            super(box, effect, cast, maxAge);
            this.strength = strength;
            this.holderId = holderId;
        }

        public Area(Identifier effectID, NbtCompound tag) {
            super(effectID, tag);
            this.strength = tag.getFloat("Strength");
            if (tag.containsUuid("HolderId")) {
                this.holderId = tag.getUuid("HolderId");
            } else {
                this.holderId = null;
            }
        }

        @Override
        public void writeToNbt(NbtCompound tag) {
            super.writeToNbt(tag);
            tag.putFloat("Strength", this.strength);
            if (this.holderId != null) {
                tag.putUuid("HolderId", this.holderId);
            }
        }

        public float getStrength() {
            return strength;
        }
    }

    private final float strength;
    private final int duration;
    private final boolean allowCaster;

    public static class Reaction extends SpellReaction {
        private final FloatModifier strength;
        private final IntModifier duration;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("magic_strength")) {
                this.strength = new FloatModifier(obj.get("magic_strength"));
            } else {
                this.strength = FloatModifier.NOOP;
            }
            if (obj.has("duration")) {
                this.duration = new IntModifier(obj.get("duration"));
            } else {
                this.duration = IntModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof WardEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("magic_strength", this.strength.toJson());
            obj.add("duration", this.duration.toJson());
        }
    }

    public WardEffect(Identifier id) {
        super(id);
        this.strength = 1;
        this.duration = 30 * 20;
        this.allowCaster = false;
    }

    public WardEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("strength")) {
            this.strength = obj.get("strength").getAsFloat();
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
    protected @NotNull ActionResult useOnBlock(ServerSpellCast cast, SimpleUseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        var duration = this.duration;
        var magStrength = this.strength;
        for (var reaction : reactions) {
            if (reaction instanceof Reaction r) {
                duration = r.duration.applyMultiple(duration, used.reactionStackSize);
                magStrength = r.strength.applyMultiple(magStrength, used.reactionStackSize);
            }
        }
        for (var reaction : used.getGlobalReactions()) {
            if (reaction instanceof Reaction r) {
                duration = r.duration.applyMultiple(duration, used.reactionStackSize);
                magStrength = r.strength.applyMultiple(magStrength, used.reactionStackSize);
            }
        }

        MagicEffectsComponent.addGlobalArea(world,
                new Area(new BlockBox(pos), this, cast, duration,
                        magStrength, this.allowCaster ? cast.getCasterUuid() : null));
        return ActionResult.SUCCESS;
    }

    public static boolean isBlockProtected(World world, BlockPos pos) {
        var comp = world.getChunk(pos).getComponent(ModComponents.CHUNK_MAGIC_EFFECTS);
        var areas = comp.getAreas(pos, WardEffect.Area.class);
        return areas.size() != 0;
    }

    public static boolean isBlockProtected(World world, BlockPos pos, @Nullable PlayerEntity player) {
        var comp = world.getChunk(pos).getComponent(ModComponents.CHUNK_MAGIC_EFFECTS);
        var areas = comp.getAreas(pos, WardEffect.Area.class);

        if (player == null && areas.size() > 0) {
            return true;
        }

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
