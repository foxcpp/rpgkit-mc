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
import org.jetbrains.annotations.Nullable;

public class PushAwayEffect extends SpellEffect {
    public static class Reaction extends SpellReaction {
        public final double velocity;

        protected Reaction(Identifier id) {
            this(id, 0);
        }

        protected Reaction(Identifier id, double velocity) {
            super(id);
            this.velocity = velocity;
        }

        @Override
        public SpellReaction withParametersFromJSON(JsonObject jsonObject) {
            var velocity = this.velocity;
            if (jsonObject.has("velocity")) {
                velocity = jsonObject.get("velocity").getAsDouble();
            }
            return new Reaction(this.id, velocity);
        }

        @Override
        public JsonObject parametersToJSON() {
            var obj = new JsonObject();
            obj.addProperty("velocity", this.velocity);
            return obj;
        }
    }

    private final double velocity;

    public PushAwayEffect() {
        this(3);
    }

    @Override
    public @Nullable SpellReaction reactionType(Identifier id) {
        return new Reaction(id);
    }

    public PushAwayEffect(double velocity) {
        this.velocity = velocity;
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
    public SpellEffect withParametersFromJSON(JsonObject jsonObject) {
        var velocity = this.velocity;
        if (jsonObject.has("velocity")) {
            velocity = jsonObject.get("velocity").getAsDouble();
        }
        return new PushAwayEffect(velocity);
    }

    @Override
    public JsonObject parametersToJSON() {
        var obj = super.parametersToJSON();
        obj.addProperty("velocity", this.velocity);
        return obj;
    }
}
