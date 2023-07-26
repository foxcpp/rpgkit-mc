package com.github.foxcpp.rpgkitmc.magic.effects.use;

import com.github.foxcpp.rpgkitmc.magic.events.MagicEntityEvents;
import com.github.foxcpp.rpgkitmc.magic.effects.SpellEffect;
import com.github.foxcpp.rpgkitmc.magic.effects.UseEffect;
import com.github.foxcpp.rpgkitmc.magic.json.IntModifier;
import com.github.foxcpp.rpgkitmc.magic.spell.ServerSpellCast;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellBuildCondition;
import com.github.foxcpp.rpgkitmc.magic.spell.SpellReaction;
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
        private final IntModifier damageDealt;

        public Reaction(JsonObject obj) {
            super(Type.EFFECT, obj);
            if (obj.has("damage")) {
                this.damageDealt = new IntModifier(obj.get("damage"));
            } else {
                this.damageDealt = IntModifier.NOOP;
            }
        }

        @Override
        public boolean appliesTo(SpellEffect effect) {
            return effect instanceof DamageEffect;
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.add("damage", this.damageDealt.toJson());
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
        private final int damage;

        protected Used(SpellBuildCondition.Context ctx) {
            super(DamageEffect.this, new ArrayList<>(), new ArrayList<>(), ctx);

            var dmg = DamageEffect.this.damageDealt;
            for (var reaction : this.effectReactions) {
                if (reaction instanceof Reaction r) {
                    dmg = r.damageDealt.applyMultiple(dmg, ctx.stackSize);
                }
            }
            if (dmg <= 1) {
                dmg = 1;
            }
            this.damage = dmg;
        }

        protected Used(JsonObject obj) {
            super(DamageEffect.this, obj);
            this.damage = obj.get("damage").getAsInt();
        }

        @Override
        public void toJson(@NotNull JsonObject obj) {
            super.toJson(obj);
            obj.addProperty("damage", this.damage);
        }

        @Override
        public @NotNull ActionResult useOnBlock(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction direction) {
            return ActionResult.PASS;
        }

        @Override
        public @NotNull ActionResult useOnEntity(ServerSpellCast cast, Entity entity) {
            if (!(entity instanceof LivingEntity le)) {
                return ActionResult.PASS;
            }

            var entityResult = MagicEntityEvents.DAMAGE.invoker().onEntityMagicDamaged(cast, this, le, this.damage);
            if (entityResult.getResult().equals(ActionResult.FAIL) || entityResult.getResult().equals(ActionResult.CONSUME)
                    || entityResult.getResult().equals(ActionResult.CONSUME_PARTIAL)) {
                return entityResult.getResult();
            }

            le.damage(DamageSource.MAGIC, entityResult.getValue());
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public UseEffect.@NotNull Used use(SpellBuildCondition.Context ctx) {
        return new Used(ctx);
    }

    @Override
    public UseEffect.@NotNull Used usedFromJson(JsonObject obj) {
        return new Used(obj);
    }

    @Override
    public String toString() {
        return "DamageEffect[" +
                "damage=" + damageDealt +
                ']';
    }
}
