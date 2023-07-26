package com.github.foxcpp.rpgkitmc.magic.form;

import com.github.foxcpp.rpgkitmc.magic.items.ModItems;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellForm;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.NotNull;

public class HitscanForm extends SpellForm {
    public HitscanForm() {
        super(ImmutableMap.of(), ImmutableMap.of());
    }

    protected boolean canHit(Entity entity) {
        return !entity.isSpectator() && entity.isAlive() && entity.canHit();
    }

    @Override
    public void startCast(@NotNull ServerSpellCast cast, ServerWorld world, @NotNull Entity caster) {
        super.startCast(cast, world, caster);

        var pos = caster.getPos();
        if (caster instanceof PlayerEntity pe) {
            pos = pos.add(pe.getHandPosOffset(ModItems.SPELL_ITEM));
            pos = pos.add(0, 0.7, 0);
        }

        var end = pos.add(caster.getRotationVector().multiply(50));
        BlockHitResult hitResult = world.raycast(new RaycastContext(
                pos, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE,
                caster));
        if (hitResult.getType() != HitResult.Type.MISS) {
            end = hitResult.getPos();
        }
        var entHitResult = ProjectileUtil.getEntityCollision(world, caster, pos, end,
                new Box(pos, end), this::canHit);
        if (entHitResult != null && entHitResult.getType() != HitResult.Type.MISS) {
            cast.getSpell().useOnEntity(cast, entHitResult.getEntity());
        } else if (hitResult.getType() != HitResult.Type.MISS) {
            cast.getSpell().useOnBlock(cast, world, hitResult.getBlockPos(), hitResult.getSide());
        }
        // TODO: Pass-through for both entities and blocks.
    }
}
