package com.github.sweetsnowywitch.csmprpgkit.magic.effects;

import com.github.sweetsnowywitch.csmprpgkit.magic.ServerSpellCast;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellEffect;
import com.github.sweetsnowywitch.csmprpgkit.magic.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageEffect extends SpellEffect {
    public static class Reaction extends SpellReaction {
        private final int damageDealt;

        public Reaction(Identifier id) {
            super(id);
            this.damageDealt = 0;
        }

        public Reaction(Identifier id, JsonObject obj) {
            super(id, obj);

            if (obj.has("damage_dealt")) {
                this.damageDealt = obj.get("damage_dealt").getAsInt();
            } else {
                this.damageDealt = 0;
            }
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("damage_dealt", this.damageDealt);
        }

        @Override
        public String toString() {
            return "DamageEffect.Reaction[" +
                    "damageDealt=" + damageDealt +
                    ']';
        }
    }

    private final int damageDealt;

    public DamageEffect(Identifier id) {
        super(id);
        this.damageDealt = 2;
    }

    public DamageEffect(Identifier id, JsonObject obj) {
        super(id);

        if (obj.has("damage_dealt")) {
            this.damageDealt = obj.get("damage_dealt").getAsInt();
        } else {
            this.damageDealt = 0;
        }
    }

    @Override
    public boolean onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        if (!(entity instanceof LivingEntity le)) {
            return false;
        }

        var damageDealt = this.damageDealt;

        for (var reaction : cast.getReactions()) {
            if (reaction instanceof DamageEffect.Reaction r) {
                damageDealt += r.damageDealt;
            }
        }

        le.damage(DamageSource.MAGIC, damageDealt);
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
    public String toString() {
        return "DamageEffect[" +
                "damage=" + damageDealt +
                ']';
    }
}
