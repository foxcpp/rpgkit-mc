package com.github.foxcpp.rpgkitmc.magic.events;

import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.NotNull;

public class MagicEntityEvents {
    /**
     * Invoked when spell damages an entity. The callback can change the damage or cancel the action altogether.
     */
    public static final Event<Damage> DAMAGE = EventFactory.createArrayBacked(Damage.class,
            (cast, effect, entity, dmg) -> TypedActionResult.pass(dmg),
            (listeners) -> (ServerSpellCast cast, UseEffect.Used effect, Entity entity, int damage) -> {
                for (var listener : listeners) {
                    var result = listener.onEntityMagicDamaged(cast, effect, entity, damage);

                    if (result.getResult() != ActionResult.PASS) {
                        return result;
                    }
                    damage = result.getValue();
                }

                return TypedActionResult.pass(damage);
            });

    public static final Event<Move> MOVE = EventFactory.createArrayBacked(Move.class,
            (cast, effect, entity, factor) -> TypedActionResult.pass(factor),
            (listeners) -> (ServerSpellCast cast, UseEffect.Used effect, Entity entity, double factor) -> {
                for (var listener : listeners) {
                    var result = listener.onEntityMagicMoved(cast, effect, entity, factor);

                    if (result.getResult() != ActionResult.PASS) {
                        return result;
                    }
                    factor = result.getValue();
                }

                return TypedActionResult.pass(factor);
            });

    public interface Damage {
        @NotNull TypedActionResult<Integer> onEntityMagicDamaged(ServerSpellCast cast, UseEffect.Used effect, Entity entity, int damage);
    }

    public interface Move {
        @NotNull TypedActionResult<Double> onEntityMagicMoved(ServerSpellCast cast, UseEffect.Used effect, Entity entity, double velocityFactor);
    }
}
