package tf.bug.alymod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.entity.AmethystBoltEntity;
import tf.bug.alymod.item.AmethystBolt;
import tf.bug.alymod.item.MonkSoul;
import tf.bug.alymod.network.MonkAttachmentUpdatePayload;

@Environment(EnvType.CLIENT)
public class AlymodClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AmethystBolt.registerClient();
        MonkSoul.registerClient();

        AmethystBoltEntity.registerClient();

        PrismaticFluid.registerClient();

        MonkAttachmentUpdatePayload.registerClient();
    }

}
