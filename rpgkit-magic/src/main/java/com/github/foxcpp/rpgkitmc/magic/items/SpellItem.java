package com.github.foxcpp.rpgkitmc.magic.items;

import com.github.foxcpp.rpgkitmc.magic.client.render.SpellItemRenderer;
import com.github.foxcpp.rpgkitmc.magic.components.ModComponents;
import com.github.foxcpp.rpgkitmc.magic.spell.Spell;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpellItem extends MagicChannelingItem implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().then("animation.model.idle", Animation.LoopType.LOOP);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public SpellItem(Settings settings) {
        super(settings);

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericIdleController(this));
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private SpellItemRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SpellItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
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
