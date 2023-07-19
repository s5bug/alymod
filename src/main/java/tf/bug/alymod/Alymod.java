package tf.bug.alymod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.sound.SoundCategory;
import tf.bug.alymod.advancement.HearPrismaticIceCriterion;
import tf.bug.alymod.advancement.InteractPrismaticIceMerchantCriterion;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.block.PrismaticFluidBlock;
import tf.bug.alymod.block.PrismaticIce;
import tf.bug.alymod.effect.ChromaticAberrationStatusEffect;
import tf.bug.alymod.entity.AmethystBoltEntity;
import tf.bug.alymod.item.*;
import tf.bug.alymod.network.ImpulseJumpMessage;

public class Alymod implements ModInitializer {

    public static final String ID = "alymod";

    @Override
    public void onInitialize() {
        HearPrismaticIceCriterion.register();
        InteractPrismaticIceMerchantCriterion.register();

        ChromaticAberrationStatusEffect.register();

        AmethystBolt.register();
        BoltSmithingTemplate.register();

        PrismaticFluid.register();
        PrismaticFluidBlock.register();

        EclipticClaw.register();
        PrismaticFluidBucket.register();
        PrismaticIce.register();
        PrismaticShard.register();

        AmethystBoltEntity.register();


        ImpulseJumpMessage.register();
    }

}
