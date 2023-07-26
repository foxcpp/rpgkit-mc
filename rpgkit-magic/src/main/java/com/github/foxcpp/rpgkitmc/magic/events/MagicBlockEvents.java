package com.github.foxcpp.rpgkitmc.magic.events;

import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class MagicBlockEvents {
    /**
     * Invoked when spell affects a block in a potentially destructive way. This includes
     * moving the block around but does not include any interaction that affects the
     * block contents (e.g. for chests). INTERACT will be used for this.
     */
    public static final Event<Damage> DAMAGE = EventFactory.createArrayBacked(Damage.class,
            (cast, effect, world, pos) -> ActionResult.PASS, (listeners) -> (ServerSpellCast cast, UseEffect.Used effect, ServerWorld world, BlockPos pos) -> {
                for (var listener : listeners) {
                    ActionResult result = listener.onBlockMagicDamaged(cast, effect, world, pos);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    /**
     * Invoked when spell affects a block in a non-destructive way. E.g. adding fuel to the furnace.
     */
    public static final Event<Interact> INTERACT = EventFactory.createArrayBacked(Interact.class,
            (cast, effect, world, pos) -> ActionResult.PASS, (listeners) -> (ServerSpellCast cast, UseEffect.Used effect, ServerWorld world, BlockPos pos) -> {
                for (var listener : listeners) {
                    ActionResult result = listener.onBlockMagicInteract(cast, effect, world, pos);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    public interface Damage {
        @NotNull ActionResult onBlockMagicDamaged(ServerSpellCast cast, UseEffect.Used effect, ServerWorld world, BlockPos pos);
    }

    public interface Interact {
        @NotNull ActionResult onBlockMagicInteract(ServerSpellCast cast, UseEffect.Used effect, ServerWorld world, BlockPos pos);
    }
}
