package tf.bug.alymod.advancement;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;

public class InteractPrismaticIceMerchantCriterion extends AbstractCriterion<InteractPrismaticIceMerchantCriterion.Condition> {

    public static final Identifier ID = Identifier.of(Alymod.ID, "interact_prismatic_ice_merchant");

    public static final InteractPrismaticIceMerchantCriterion INSTANCE =
            new InteractPrismaticIceMerchantCriterion();

    @Override
    public Codec<Condition> getConditionsCodec() {
        return Condition.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        trigger(player, condition -> {
            return true;
        });
    }

    public static void register() {
        Registry.register(Registries.CRITERION, InteractPrismaticIceMerchantCriterion.ID, InteractPrismaticIceMerchantCriterion.INSTANCE);
    }

    public static final record Condition(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<Condition> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Condition::player)).apply(instance, Condition::new);
        });
    }

}
