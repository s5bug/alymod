package tf.bug.alymod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Items;
import tf.bug.alymod.advancement.HearPrismaticIceCriterion;
import tf.bug.alymod.advancement.InteractPrismaticIceMerchantCriterion;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.block.PrismaticFluidBlock;
import tf.bug.alymod.block.PrismaticFluidBlockEntity;
import tf.bug.alymod.block.PrismaticIce;
import tf.bug.alymod.entity.AmethystBoltEntity;
import tf.bug.alymod.item.AmethystBolt;
import tf.bug.alymod.item.BoltSmithingTemplate;
import tf.bug.alymod.item.PrismaticFluidBucket;
import tf.bug.alymod.item.PrismaticShard;

public class Alymod implements ModInitializer {

    public static final String ID = "alymod";

    @Override
    public void onInitialize() {
        HearPrismaticIceCriterion.register();
        InteractPrismaticIceMerchantCriterion.register();

        AmethystBolt.register();
        BoltSmithingTemplate.register();

        PrismaticFluid.register();
        PrismaticFluidBlock.register();
        PrismaticFluidBlockEntity.register();

        PrismaticFluidBucket.register();
        PrismaticIce.register();
        PrismaticShard.register();

        AmethystBoltEntity.register();
    }

}
