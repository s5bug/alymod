package tf.bug.alymod.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class InteractPrismaticIceMerchantCriterion extends AbstractCriterion<InteractPrismaticIceMerchantCriterion.Condition> {

    public static final Identifier ID = new Identifier(Alymod.ID, "interact_prismatic_ice_merchant");

    public static final InteractPrismaticIceMerchantCriterion INSTANCE =
            new InteractPrismaticIceMerchantCriterion();

    @Override
    protected InteractPrismaticIceMerchantCriterion.Condition conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new InteractPrismaticIceMerchantCriterion.Condition();
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
        Criteria.register(InteractPrismaticIceMerchantCriterion.INSTANCE);
    }

    public static class Condition extends AbstractCriterionConditions {

        public Condition() {
            super(ID, LootContextPredicate.EMPTY);
        }

    }

}
