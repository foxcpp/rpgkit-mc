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
import org.jetbrains.annotations.Nullable;

public class DamageEffect extends SpellEffect {

    public static class Reaction extends SpellReaction {
        private final int damageDealt;

        protected Reaction(Identifier id) {
            this(id, 0);
        }

        protected Reaction(Identifier id, int damageDealt) {
            super(id);
            this.damageDealt = damageDealt;
        }

        @Override
        public SpellReaction withParametersFromJSON(JsonObject jsonObject) {
            var damageDealt = this.damageDealt;
            if (jsonObject.has("damage_dealt")) {
                damageDealt = jsonObject.get("damage_dealt").getAsInt();
            }

            var r = new Reaction(this.id, damageDealt);
            r.populateFromJson(jsonObject);
            return r;
        }

        @Override
        public JsonObject parametersToJSON() {
            var obj = new JsonObject();
            obj.addProperty("damage_dealt", this.damageDealt);
            return obj;
        }

        @Override
        public String toString() {
            return "Reaction[" +
                    "damageDealt=" + damageDealt +
                    ']';
        }
    }


    private final int damageDealt;

    public DamageEffect() {
        this.damageDealt = 0;
    }

    public DamageEffect(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    @Override
    public @Nullable SpellReaction reactionType(Identifier id) {
        return new Reaction(id);
    }

    @Override
    public void onSingleEntityHit(ServerSpellCast cast, Entity entity) {
        if (!(entity instanceof LivingEntity le)) {
            return;
        }

        var damageDealt = this.damageDealt;

        for (var reaction : cast.getEffectReactions()) {
            if (reaction.appliesTo(this)) {
                var deReaction = (DamageEffect.Reaction) reaction;
                damageDealt += deReaction.damageDealt;
            }
        }

        le.damage(DamageSource.MAGIC, damageDealt);
    }

    @Override
    public void onSingleBlockHit(ServerSpellCast cast, ServerWorld world, BlockPos pos, Direction dir) {

    }

    @Override
    public void onAreaHit(ServerSpellCast cast, ServerWorld world, Box box) {

    }

    @Override
    public SpellEffect withParametersFromJSON(JsonObject obj) {
        int damageDealt = this.damageDealt;
        if (obj.has("damage_dealt")) {
            damageDealt = obj.get("damage_dealt").getAsInt();
        }

        return new DamageEffect(damageDealt);
    }
}
