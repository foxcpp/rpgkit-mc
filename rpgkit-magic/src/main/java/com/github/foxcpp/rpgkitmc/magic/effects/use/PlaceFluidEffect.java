package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.json.BlockStatePredicate;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceFluidEffect extends SimpleUseEffect {
    private final FluidState state;
    private final boolean replace;
    private final @Nullable BlockStatePredicate filter;

    public PlaceFluidEffect(Identifier id) {
        super(id);
        this.state = Fluids.FLOWING_WATER.getDefaultState();
        this.replace = false;
        this.filter = null;
    }

    public PlaceFluidEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("fluid")) {
            var fluidId = new Identifier(obj.get("fluid").getAsString());
            var fluid = Registries.FLUID.get(fluidId);
            if (obj.has("level")) {
                if (!(fluid instanceof FlowableFluid ff)) {
                    throw new IllegalArgumentException("level can be set only for flowable fluid, %s is not".formatted(fluidId));
                }
                this.state = ff.getFlowing(obj.get("level").getAsInt(), false);
            } else {
                if (fluid instanceof FlowableFluid ff) {
                    this.state = ff.getStill(false);
                } else {
                    this.state = fluid.getDefaultState();
                }
            }
        } else if (obj.has("fluidstate")) {
            this.state = FluidState.CODEC.parse(JsonOps.INSTANCE, obj.get("fluidstate")).result().orElseThrow();
        } else {
            this.state = Fluids.FLOWING_WATER.getDefaultState();
        }

        this.replace = obj.has("replace") && obj.get("replace").getAsBoolean();

        if (obj.has("filter")) {
            this.filter = new BlockStatePredicate(obj.get("filter"));
        } else {
            this.filter = null;
        }
    }

    @Override
    protected @NotNull ActionResult useOnBlock(ServerSpellCast cast, SimpleUseEffect.Used used, ServerWorld world, BlockPos pos, Direction direction, List<SpellReaction> reactions) {
        BlockPos target;
        if (this.replace) {
            target = pos;
        } else {
            target = pos.add(direction.getVector());
        }

        if (this.filter != null && !this.filter.test(world.getBlockState(target))) {
            return ActionResult.PASS;
        }

        if (!world.isInBuildLimit(pos)) {
            return ActionResult.PASS;
        }

        var playerCaster = cast.getPlayerCaster(world);
        if (playerCaster != null && !world.canPlayerModifyAt(playerCaster, pos)) {
            return ActionResult.PASS;
        }

        var ok = this.placeFluid(cast.getPlayerCaster(world), world, pos, Direction.UP);
        return ok ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    @Override
    protected @NotNull ActionResult useOnEntity(ServerSpellCast cast, SimpleUseEffect.Used used, Entity entity, List<SpellReaction> reactions) {
        var world = (ServerWorld) entity.getWorld();

        BlockPos target;
        if (entity.isOnGround() && this.replace) {
            target = entity.getBlockPos().add(0, -1, 0);
        } else {
            target = entity.getBlockPos();
        }

        if (this.filter != null && !this.filter.test(world.getBlockState(target))) {
            return ActionResult.PASS;
        }

        var ok = this.placeFluid(cast.getPlayerCaster(world), world, entity.getBlockPos(), Direction.UP);
        return ok ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    public boolean placeFluid(@Nullable PlayerEntity player, World world, BlockPos pos, @Nullable Direction side) {
        var fluid = this.state.getFluid();

        if (!(fluid instanceof FlowableFluid)) {
            return false;
        }
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();
        boolean canFill = blockState.isAir() ||
                blockState.canBucketPlace(fluid) ||
                block instanceof FluidFillable &&
                        ((FluidFillable) block).canFillWithFluid(world, pos, blockState, fluid);
        if (!canFill) {
            return side != null && this.placeFluid(player, world, pos.offset(side), null);
        }
        if (world.getDimension().ultrawarm() && this.state.isIn(FluidTags.WATER)) {
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5f,
                    2.6f + (world.random.nextFloat() - world.random.nextFloat()) * 0.8f);
            return true;
        }

        if (block instanceof FluidFillable && fluid == Fluids.WATER) {
            ((FluidFillable) block).tryFillWithFluid(world, pos, blockState, this.state);
            return true;
        }
        if (blockState.canBucketPlace(fluid) && !material.isLiquid()) {
            world.breakBlock(pos, true);
        }
        return world.setBlockState(pos, this.state.getBlockState(),
                Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD) || blockState.getFluidState().isStill();
    }


    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.add("fluidstate", FluidState.CODEC.encodeStart(JsonOps.INSTANCE, this.state).result().orElseThrow());
        obj.addProperty("replace", this.replace);
        if (this.filter != null) {
            obj.add("filter", this.filter.toJson());
        }
    }
}
