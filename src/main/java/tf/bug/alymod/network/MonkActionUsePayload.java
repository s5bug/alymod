package tf.bug.alymod.network;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.monk.MonkAction;

public record MonkActionUsePayload(int actionIndex, List<Integer> targets) implements CustomPayload {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "monk_action");

    public static final Id<MonkActionUsePayload> PAYLOAD_ID =
            new CustomPayload.Id<>(MonkActionUsePayload.ID);

    public static final PacketCodec<RegistryByteBuf, MonkActionUsePayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    MonkActionUsePayload::actionIndex,
                    PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER),
                    MonkActionUsePayload::targets,
                    MonkActionUsePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return MonkActionUsePayload.PAYLOAD_ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(MonkActionUsePayload.PAYLOAD_ID, MonkActionUsePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MonkActionUsePayload.PAYLOAD_ID, (payload, context) -> {
            MonkAction.values()[payload.actionIndex].executeServer(
                    context.player(),
                    payload.targets.stream().map(context.player().getServerWorld()::getDragonPart).toList()
            );
        });
    }
}
