package tf.bug.alymod.client;

import net.fabricmc.api.ClientModInitializer;
import tf.bug.alymod.block.PrismaticFluid;
import tf.bug.alymod.entity.AmethystBoltEntity;
import tf.bug.alymod.item.AmethystBolt;

public class AlymodClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AmethystBolt.registerClient();

        AmethystBoltEntity.registerClient();

        PrismaticFluid.registerClient();
    }

}
