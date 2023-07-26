package com.github.foxcpp.rpgkitmc.magic.items;

import com.github.foxcpp.rpgkitmc.magic.RPGKitMagicMod;
import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public abstract class MagicChannelingItem extends Item {
    public MagicChannelingItem(Settings settings) {
        super(settings);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        var nbt = stack.getNbt();
        if (nbt == null) {
            return 0;
        }
        return nbt.getInt("MaxChannelAge");
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return super.postHit(stack, target, attacker);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient) {
            context.getPlayer().setCurrentHand(context.getHand());
            return ActionResult.SUCCESS;
        }

        try {
            var res = context.getPlayer().getComponent(ModComponents.CAST).performCastOnBlock(context.getBlockPos(), context.getSide());
            if (res.equals(ActionResult.FAIL) || res.equals(ActionResult.PASS)) {
                context.getPlayer().sendMessage(Text.translatable("rpgkit.magic.no_effect_spell_cast"), true);
            }
            return res;
        } catch (Exception ex) {
            RPGKitMagicMod.LOGGER.error("Exception happening while trying to perform cast", ex);
        }

        context.getPlayer().setCurrentHand(context.getHand());
        return ActionResult.FAIL;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.world.isClient) {
            user.setCurrentHand(hand);
            return ActionResult.SUCCESS;
        }

        try {
            var res = user.getComponent(ModComponents.CAST).performCastOnEntity(entity);
            if (res.equals(ActionResult.FAIL) || res.equals(ActionResult.PASS)) {
                user.sendMessage(Text.translatable("rpgkit.magic.no_effect_spell_cast"), true);
            }
            return res;
        } catch (Exception ex) {
            RPGKitMagicMod.LOGGER.error("Exception happening while trying to perform cast", ex);
        }

        user.setCurrentHand(hand);
        return ActionResult.FAIL;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack inHand = user.getStackInHand(hand);
        if (!world.isClient) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(inHand);
        }

        try {
            return new TypedActionResult<>(user.getComponent(ModComponents.CAST).performRangedCast(), inHand);
        } catch (Exception ex) {
            RPGKitMagicMod.LOGGER.error("Exception happened while trying to perform cast", ex);
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(inHand);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient) {
            return;
        }

        try {
            user.getComponent(ModComponents.CAST).interruptChanneling();
        } catch (Exception ex) {
            RPGKitMagicMod.LOGGER.error("Exception happened while trying to interrupt cast", ex);
        }
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(entity instanceof ServerPlayerEntity spe)) {
            return;
        }
        var comp = spe.getComponent(ModComponents.CAST);

        if (!comp.isBuilding() && !comp.isChanneling()) {
            return;
        }

        if (!selected) {
            if (comp.isBuilding()) {
                comp.performSelfCast();
            } else if (comp.isChanneling()) {
                comp.interruptChanneling();
            }
        }
    }
}
