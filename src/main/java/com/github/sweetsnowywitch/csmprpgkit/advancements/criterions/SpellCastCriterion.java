package com.github.sweetsnowywitch.csmprpgkit.advancements.criterions;

import com.github.sweetsnowywitch.csmprpgkit.RPGKitMod;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.UsingItemCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityPredicate.Extended;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class SpellCastCriterion extends AbstractCriterion<SpellCastCriterion.Conditions> {
    static Identifier id = null;

    public SpellCastCriterion(Identifier id) {
        SpellCastCriterion.id = id;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Conditions conditionsFromJson(JsonObject obj, Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        ItemPredicate itemPredicate = ItemPredicate.fromJson(obj.get("item"));
        return new Conditions(playerPredicate, itemPredicate);
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, (conditions) -> true);
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final ItemPredicate item;

        public Conditions(EntityPredicate.Extended player, ItemPredicate item) {
            super(SpellCastCriterion.id, player);
            this.item = item;
        }

        public static Conditions create(EntityPredicate.Builder player, ItemPredicate.Builder item) {
            return new Conditions(Extended.ofLegacy(player.build()), item.build());
        }

        public boolean test(ItemStack stack) {
            return this.item.test(stack);
        }

        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("item", this.item.toJson());
            return jsonObject;
        }
    }
}
