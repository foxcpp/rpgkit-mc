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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnEntityEffect extends SimpleUseEffect {
    private final @Nullable NbtCompound customNbt;
    private final boolean inBlock;
    private final EntityType<?> entityType;

    public SpawnEntityEffect(Identifier id) {
        super(id);
        this.customNbt = new NbtCompound();
        this.inBlock = false;
        this.entityType = EntityType.PIG;
    }

    public SpawnEntityEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        this.entityType = Registry.ENTITY_TYPE.get(new Identifier(obj.get("id").getAsString()));
        if (obj.has("custom_nbt")) {
            this.customNbt = NbtCompound.CODEC.parse(JsonOps.INSTANCE, obj.get("custom_nbt")).result().orElseThrow();
        } else {
            this.customNbt = null;
        }

        this.inBlock = obj.has("in_block") && obj.get("in_block").getAsBoolean();
    }


    @Override
    protected @NotNull ActionResult useOnBlock(ServerSpellCast cast, SimpleUseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        if (!this.inBlock) {
            pos = pos.add(direction.getVector());
        }

        var ent = this.entityType.create(world, this.customNbt, null, null, pos, SpawnReason.MOB_SUMMONED, true, false);
        if (ent == null) {
            return ActionResult.PASS;
        }
        world.spawnEntity(ent);
        return ActionResult.SUCCESS;
    }

    @Override
    protected @NotNull ActionResult useOnEntity(ServerSpellCast cast, SimpleUseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        var world = (ServerWorld) entity.getWorld();
        var ent = this.entityType.create(world, this.customNbt, null, null, entity.getBlockPos(), SpawnReason.MOB_SUMMONED, true, false);
        if (ent == null) {
            return ActionResult.PASS;
        }
        world.spawnEntity(ent);
        return ActionResult.SUCCESS;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        if (this.customNbt != null) {
            obj.add("custom_nbt", NbtCompound.CODEC.encodeStart(JsonOps.INSTANCE, this.customNbt).result().orElseThrow());
        }
        obj.addProperty("in_block", this.inBlock);
        obj.addProperty("id", Registry.ENTITY_TYPE.getId(this.entityType).toString());
    }
}
