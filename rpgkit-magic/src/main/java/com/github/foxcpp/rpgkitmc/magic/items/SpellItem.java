package com.github.foxcpp.rpgkitmc.magic.items;

import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class SpellItem extends MagicChannelingItem implements IAnimatable {
    public AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public SpellItem(Settings settings) {
        super(settings);
    }

    private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.idle", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        var cont = new AnimationController<>(this, "controller", 20, this::predicate);
        data.addAnimationController(cont);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        var nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("SpellTranslationKey")) {
            return "rpgkit.magic.spell_build";
        }
        return nbt.getString("SpellTranslationKey");
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(entity instanceof ServerPlayerEntity spe)) {
            return;
        }
        var comp = spe.getComponent(ModComponents.CAST);

        if (!comp.isBuilding() && !comp.isChanneling()) {
            stack.decrement(1);
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
