package tf.bug.alymod;

import java.util.PriorityQueue;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tf.bug.alymod.advancement.HearPrismaticIceCriterion;
import tf.bug.alymod.advancement.InteractPrismaticIceMerchantCriterion;
import tf.bug.alymod.attachment.PlayerMonkAttachments;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.block.PrismaticFluidBlock;
import tf.bug.alymod.block.PrismaticIce;
import tf.bug.alymod.effect.*;
import tf.bug.alymod.entity.AmethystBoltEntity;
import tf.bug.alymod.imixin.IMinecraftServerExtension;
import tf.bug.alymod.item.*;
import tf.bug.alymod.monk.MonkAction;
import tf.bug.alymod.network.EclipticClawUsePayload;
import tf.bug.alymod.network.ImpulseJumpPayload;
import tf.bug.alymod.network.MonkActionUsePayload;
import tf.bug.alymod.network.MonkAttachmentUpdatePayload;

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
                        entries.add(MonkSoul.INSTANCE);
                    })
                    .build();

    @Override
    public void onInitialize() {
        HearPrismaticIceCriterion.register();
        InteractPrismaticIceMerchantCriterion.register();

        ChromaticAberrationStatusEffect.register();
        MonkStatusEffects.register();

        AmethystBolt.register();
        BoltSmithingTemplate.register();

        PrismaticFluid.register();
        PrismaticFluidBlock.register();

        EclipticClaw.register();
        MonkSoul.register();
        PrismaticFluidBucket.register();
        PrismaticIce.register();
        PrismaticShard.register();

        AmethystBoltEntity.register();

        EclipticClawUsePayload.register();
        ImpulseJumpPayload.register();
        MonkActionUsePayload.register();
        MonkAttachmentUpdatePayload.register();

        MonkAction.register();
        PlayerMonkAttachments.register();

        Registry.register(Registries.ITEM_GROUP, Identifier.of(Alymod.ID, "item_group"), Alymod.ITEM_GROUP);

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            IMinecraftServerExtension serverExt = (IMinecraftServerExtension) server;

            for (ServerWorld world : server.getWorlds()) {
                PriorityQueue<IMinecraftServerExtension.FutureTickRunnable> thisWorldTicks =
                        serverExt.alymod$getFutureTickRunnableQueue(world.getRegistryKey());

                long thisWorldTime = world.getTime();

                while (!thisWorldTicks.isEmpty() && thisWorldTicks.peek().target() <= thisWorldTime) {
                    thisWorldTicks.poll().action().run();
                }
            }
        });
    }

}
