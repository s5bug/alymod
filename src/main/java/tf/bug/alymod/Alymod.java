package tf.bug.alymod;

import com.mojang.datafixers.optics.Prism;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tf.bug.alymod.advancement.HearPrismaticIceCriterion;
import tf.bug.alymod.advancement.InteractPrismaticIceMerchantCriterion;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.block.PrismaticFluidBlock;
import tf.bug.alymod.block.PrismaticIce;
import tf.bug.alymod.effect.ChromaticAberrationStatusEffect;
import tf.bug.alymod.entity.AmethystBoltEntity;
import tf.bug.alymod.item.*;
import tf.bug.alymod.network.EclipticClawUseMessage;
import tf.bug.alymod.network.ImpulseJumpMessage;

public class Alymod implements ModInitializer {

    public static final String ID = "alymod";

    public static final ItemGroup ITEM_GROUP =
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(PrismaticShard.INSTANCE))
                    .displayName(Text.translatable("itemGroup.alymod.item_group"))
                    .entries((displayContext, entries) -> {
                        entries.add(PrismaticShard.INSTANCE);
                        entries.add(PrismaticIce.ITEM_INSTANCE);
                        entries.add(PrismaticFluidBucket.INSTANCE);
                        entries.add(BoltSmithingTemplate.INSTANCE);
                        entries.add(AmethystBolt.INSTANCE);
                        entries.add(EclipticClaw.INSTANCE);
                    })
                    .build();

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

        EclipticClawUseMessage.register();
        ImpulseJumpMessage.register();

        Registry.register(Registries.ITEM_GROUP, Identifier.of(Alymod.ID, "item_group"), Alymod.ITEM_GROUP);
    }

}
