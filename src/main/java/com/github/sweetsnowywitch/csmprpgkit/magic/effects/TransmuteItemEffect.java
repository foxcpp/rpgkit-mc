package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.ModRegistries;
import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.github.sweetsnowywitch.csmprpgkit.magic.ItemTransmuteMapping;
import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class TransmuteItemEffect extends SpellEffect {

    private final ItemTransmuteMapping mapping;

    protected TransmuteItemEffect(Identifier id) {
        super(id);

        this.mapping = null;

        RPGKitMod.LOGGER.warn("TransformItemEffect is not configured, will be no-op");
    }

    protected TransmuteItemEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        this.mapping = ModRegistries.TRANSMUTE_MAPPINGS.get(new Identifier(obj.get("mapping").getAsString()));
        if (this.mapping == null) {
            throw new IllegalArgumentException("transmute mapping does not exist");
        }
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        return false;
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
    }

    @Override
    public ItemStack onItemHit(ServerSpellCast cast, ServerWorld world, @Nullable Entity holder, ItemStack stack) {
        stack = super.onItemHit(cast, world, holder, stack);

        if (this.mapping == null) {
            return stack;
        }

        var lcb = new LootContext.Builder(world);
        lcb.luck(0.5f);
        lcb.parameter(LootContextParameters.ORIGIN, cast.getOriginPos());
        Entity thisEntity;
        if (holder != null) {
            thisEntity = holder;
        } else {
            thisEntity = cast.getCaster(world);
            if (thisEntity == null) {
                RPGKitMod.LOGGER.warn("Could not provide THIS_ENTITY for loot functions");
            }
        }
        lcb.parameter(LootContextParameters.THIS_ENTITY, thisEntity);
        var context = lcb.build(LootContextTypes.SELECTOR);

        return this.mapping.transmute(stack, context);
    }
}
