package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.google.gson.JsonObject;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;

public class FireEffect extends SpellEffect {
    private final float areaCoverage;

    public FireEffect(Identifier id) {
        super(id);
        this.areaCoverage = 0.25f;
    }

    public FireEffect(Identifier id, JsonObject obj) {
        super(id, obj);
        if (obj.has("area_coverage")) {
            this.areaCoverage = obj.get("area_coverage").getAsFloat();
        } else {
            this.areaCoverage = 0.25f;
        }
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        entity.setOnFireFor(5);
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        return !this.lit(cast, world, pos, dir);
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        var bb = new BlockBox(
                (int) box.minX, (int) box.minY, (int) box.minZ,
                (int) box.maxX, (int) box.maxY, (int) box.maxZ
        );

        var startPos = cast.getOriginPos();
        var pos = new BlockPos.Mutable(0, 0, 0);
        for (int y = bb.getMinY(); y <= bb.getMaxY(); y++) {
            for (int x = bb.getMinX(); x <= bb.getMaxX(); x++) {
                for (int z = bb.getMinZ(); z <= bb.getMaxZ(); z++) {
                    pos.set(x, y, z);
                    if (Math.abs(pos.getX() - startPos.getX()) <= 1 &&
                            Math.abs(pos.getY() - startPos.getY()) <= 2 &&
                            Math.abs(pos.getZ() - startPos.getZ()) <= 1) {
                        continue;
                    }
                    if (world.isAir(pos)) continue;
                    if (RPGKitMod.RANDOM.nextFloat() <= this.areaCoverage) {
                        this.lit(cast, world, pos, Direction.UP);
                    }
                }
            }
        }
    }

    private boolean lit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        BlockState blockState = world.getBlockState(pos);
        if (CampfireBlock.canBeLit(blockState) || CandleBlock.canBeLit(blockState) || CandleCakeBlock.canBeLit(blockState)) {
            world.setBlockState(pos, blockState.with(Properties.LIT, true), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_CHANGE, pos);
            return true;
        }

        pos = pos.offset(dir);
        if (AbstractFireBlock.canPlaceAt(world, pos, dir)) {
            BlockState blockState2 = AbstractFireBlock.getState(world, pos);
            world.setBlockState(pos, blockState2, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
            world.emitGameEvent(cast.getCaster(world), GameEvent.BLOCK_PLACE, pos);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FireEffect[" +
                "areaCoverage=" + areaCoverage +
                ']';
    }
}
