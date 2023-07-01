package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class SpawnEntityEffect extends SimpleUseEffect {
    private final NbtCompound definition;
    private final boolean inBlock;
    private final EntityType entityType;

    public SpawnEntityEffect(Identifier id) {
        super(id);
        this.definition = new NbtCompound();
        this.inBlock = false;
        this.entityType = EntityType.PIG;
    }

    public SpawnEntityEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        this.definition = NbtCompound.CODEC.parse(JsonOps.INSTANCE, obj).result().orElseThrow();
        if (!this.definition.contains("id")) {
            throw new IllegalArgumentException("id is required for SpawnEntityEffect");
        }

        this.inBlock = obj.has("in_block") && obj.get("in_block").getAsBoolean();
        this.entityType = Registry.ENTITY_TYPE.get(new Identifier(this.definition.getString("id")));
    }


    @Override
    protected ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        if (!this.inBlock) {
            pos = pos.add(direction.getVector());
        }

        var ent = this.entityType.create(world, this.definition, null, null, pos, SpawnReason.MOB_SUMMONED, true, false);
        if (ent == null) {
            return ActionResult.PASS;
        }
        world.spawnEntity(ent);
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult useOnEntity(ServerSpellCast cast, Entity entity, List<SpellReaction> reactions) {
        var ent = EntityType.loadEntityWithPassengers(this.definition, entity.getWorld(), ent2 -> ent2);
        if (ent == null) {
            return ActionResult.PASS;
        }
        ent.setPosition(entity.getX(), entity.getY(), entity.getZ());
        entity.getWorld().spawnEntity(ent);
        return ActionResult.SUCCESS;
    }
}
