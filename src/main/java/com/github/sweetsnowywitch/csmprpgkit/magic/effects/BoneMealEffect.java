package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;

public class BoneMealEffect extends SpellEffect {
    private final float areaCoverage;

    public BoneMealEffect(Identifier id) {
        super(id);
        this.areaCoverage = 0.95f;
    }

    public BoneMealEffect(Identifier id, JsonObject obj) {
        super(id);
        if (obj.has("area_coverage")) {
            this.areaCoverage = obj.get("area_coverage").getAsFloat();
        } else {
            this.areaCoverage = 0.25f;
        }
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        return !this.boneMeal(cast, world, pos, dir);
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        var bb = new BlockBox(
                (int) box.minX, (int) box.minY, (int) box.minZ,
                (int) box.maxX, (int) box.maxY, (int) box.maxZ
        );

        var pos = new BlockPos.Mutable(0, 0, 0);
        for (int x = bb.getMinX(); x <= bb.getMaxX(); x++) {
            for (int z = bb.getMinZ(); z <= bb.getMaxZ(); z++) {
                for (int y = bb.getMinY(); y <= bb.getMaxY(); y++) {
                    pos.set(x, y, z);
                    if (RPGKitMod.RANDOM.nextFloat() <= this.areaCoverage) {
                        this.boneMeal(cast, world, pos, Direction.SOUTH);
                    }
                    if (world.isAir(pos)) break;
                }
            }
        }
    }

    private boolean boneMeal(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction side) {
        var mockStack = new ItemStack(Items.BONE_MEAL);
        if (BoneMealItem.useOnFertilizable(mockStack, world, pos)) {
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
            return true;
        }
        BlockPos up = pos.offset(side);
        BlockState blockState = world.getBlockState(up);
        if (blockState.isSideSolidFullSquare(world, pos, side) &&
                BoneMealItem.useOnGround(mockStack, world, up, side)) {
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, up, 0);
            return true;
        }
        return false;
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        obj.addProperty("area_coverage", this.areaCoverage);
    }

    @Override
    public String toString() {
        return "BoneMealEffect[" +
                "areaCoverage=" + areaCoverage +
                ']';
    }
}
