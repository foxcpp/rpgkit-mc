package com.github.sweetsnowywitch.rpgkit.magic.effects.use;

import com.github.sweetsnowywitch.rpgkit.magic.effects.UseEffect;
import com.github.sweetsnowywitch.rpgkit.magic.spell.ServerSpellCast;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellBuildCondition;
import com.github.sweetsnowywitch.rpgkit.magic.spell.SpellReaction;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DamageEffect extends UseEffect {
    public static class Reaction extends SpellReaction {
        private final int damageDealt;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
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

    protected final int damageDealt;

    public DamageEffect(Identifier id) {
        super(id);
        this.damageDealt = 2;
    }

    public DamageEffect(Identifier id, JsonObject obj) {
        super(id, obj);

        if (obj.has("damage_dealt")) {
            this.damageDealt = obj.get("damage_dealt").getAsInt();
        } else {
            this.damageDealt = 0;
        }
    }

    public class Used extends UseEffect.Used {
        protected Used(SpellBuildCondition.Context ctx) {
            super(DamageEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);
        }

        protected Used(JsonObject obj) {
            super(DamageEffect.this, obj);
        }

        @Override
        public ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            return ActionResult.PASS;
        }

        @Override
        public ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            if (!(entity instanceof LivingEntity le)) {
                return ActionResult.PASS;
            }

            var damageDealt = DamageEffect.this.damageDealt;

            for (var reaction : this.effectReactions) {
                if (reaction instanceof DamageEffect.Reaction r) {
                    damageDealt += r.damageDealt;
                }
            }
            if (damageDealt <= 1) {
                damageDealt = 1;
            }

            le.damage(DamageSource.MAGIC, damageDealt);
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public UseEffect.Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public UseEffect.Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public String toString() {
        return "DamageEffect[" +
                "damage=" + damageDealt +
                ']';
    }
}
