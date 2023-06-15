package tf.bug.alymod.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class HearPrismaticIceCriterion extends AbstractCriterion<HearPrismaticIceCriterion.Condition> {

    public static final Identifier ID = new Identifier(Alymod.ID, "hear_prismatic_ice");

    public static final HearPrismaticIceCriterion INSTANCE =
            new HearPrismaticIceCriterion();

    @Override
    protected Condition conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new Condition();
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public void trigger(ServerPlayerEntity player) {
        trigger(player, condition -> {
            return true;
        });
    }

    public static void register() {
        Criteria.register(HearPrismaticIceCriterion.INSTANCE);
    }

    public static class Condition extends AbstractCriterionConditions {

        public Condition() {
            super(ID, LootContextPredicate.EMPTY);
        }

    }

}
