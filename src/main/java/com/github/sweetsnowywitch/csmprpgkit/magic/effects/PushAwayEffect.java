package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class PushAwayEffect extends SpellEffect {
    public static class Reaction extends SpellReaction {
        public final double velocity;

        public Reaction(Identifier id) {
            super(id);
            this.velocity = 0;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id);
            if (obj.has("velocity")) {
                this.velocity = obj.get("velocity").getAsDouble();
            } else {
                this.velocity = 0.25;
            }
        }

        public void toJson(@NotNull JsonObject obj) {
            obj.addProperty("velocity", this.velocity);
        }

        @Override
        public String toString() {
            return "PushAwayEffect.Reaction{" +
                    "velocity=" + velocity +
                    '}';
        }
    }

    private final double velocity;

    public PushAwayEffect(Identifier id) {
        super(id);
        this.velocity = 3;
    }

    public PushAwayEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("velocity")) {
            this.velocity = obj.get("velocity").getAsDouble();
        } else {
            this.velocity = 3;
        }
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        var direction = entity.getPos().subtract(cast.getStartPos()).normalize();

        entity.setVelocity(entity.getVelocity().add(direction.multiply(this.velocity)));
        return false;
    }

    @Override
    public boolean onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {
        // none
        return false;
    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {
        // none
    }

    @Override
    public void toJson(@NotNull JsonObject obj) {
        super.toJson(obj);
        obj.addProperty("velocity", this.velocity);
    }
}
