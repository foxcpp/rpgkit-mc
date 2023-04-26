package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.google.gson.JsonObject;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;

public class ExtinguishEffect extends SpellEffect {
    private final float areaCoverage;

    public ExtinguishEffect(Identifier id) {
        super(id);
        this.areaCoverage = 0.95f;
    }

    public ExtinguishEffect(Identifier id, JsonObject obj) {
        super(id);
        if (obj.has("area_coverage")) {
            this.areaCoverage = obj.get("area_coverage").getAsFloat();
        } else {
            this.areaCoverage = 0.25f;
        }
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        entity.setOnFire(false);
        entity.setOnFireFor(0);
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        return !this.extinguish(cast, world, pos);
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        var bb = new BlockBox(
                (int)box.minX, (int)box.minY, (int)box.minZ,
                (int)box.maxX, (int)box.maxY, (int)box.maxZ
        );

        var pos = new BlockPos.Mutable(0, 0, 0);
        for (int y = bb.getMinY(); y <= bb.getMaxY(); y++) {
            for (int x = bb.getMinX(); x <= bb.getMaxX(); x++) {
                for (int z = bb.getMinZ(); z <= bb.getMaxZ(); z++) {
                    pos.set(x, y, z);
                    if (world.isAir(pos)) continue;
                    if (RPGKitMod.RANDOM.nextFloat() <= this.areaCoverage) {
                        this.extinguish(cast, world, pos);
                    }
                }
            }
        }
    }

    private boolean extinguish(ServerSpellCast cast, ServerWorld world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (CampfireBlock.isLitCampfire(blockState) || CandleBlock.isLitCandle(blockState) || CandleCakeBlock.isLitCandle(blockState)) {
            world.setBlockState(pos, blockState.with(Properties.LIT, false), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_CHANGE, pos);
            return true;
        }

        if (blockState.isIn(BlockTags.FIRE)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_DESTROY, pos);
            return true;
        }
        return false;
    }

    public void toJson(@NotNull JsonObject obj) {
        obj.addProperty("area_coverage", this.areaCoverage);
    }

    @Override
    public String toString() {
        return "ExtinguishEffect[" +
                "areaCoverage=" + areaCoverage +
                ']';
    }
}
