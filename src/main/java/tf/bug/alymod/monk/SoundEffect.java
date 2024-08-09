package tf.bug.alymod.monk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface SoundEffect {

    @Environment(EnvType.CLIENT)
    void clientOnSnapshotSelf(ClientPlayerEntity cpe);
    @Environment(EnvType.CLIENT)
    void clientOnSnapshotTarget(ClientPlayerEntity cpe, Entity target);

    void serverOnSnapshotSelf(ServerPlayerEntity pe);
    void serverOnSnapshotTarget(ServerPlayerEntity pe, Entity target);

}
