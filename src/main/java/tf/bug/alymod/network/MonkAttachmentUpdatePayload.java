package tf.bug.alymod.network;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.attachment.PlayerMonkAttachments;

public record MonkAttachmentUpdatePayload(int greasedLightning, long greasedLightningExpires, int chakra) implements CustomPayload {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "monk_attachment_update");

    public static final Id<MonkAttachmentUpdatePayload> PAYLOAD_ID =
        new CustomPayload.Id<>(MonkAttachmentUpdatePayload.ID);

    public static final PacketCodec<RegistryByteBuf, MonkAttachmentUpdatePayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    MonkAttachmentUpdatePayload::greasedLightning,
                    PacketCodecs.VAR_LONG,
                    MonkAttachmentUpdatePayload::greasedLightningExpires,
                    PacketCodecs.INTEGER,
                    MonkAttachmentUpdatePayload::chakra,
                    MonkAttachmentUpdatePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return MonkAttachmentUpdatePayload.PAYLOAD_ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(MonkAttachmentUpdatePayload.PAYLOAD_ID, MonkAttachmentUpdatePayload.CODEC);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(MonkAttachmentUpdatePayload.PAYLOAD_ID, (payload, context) -> {
            context.player().setAttached(PlayerMonkAttachments.greasedLightning(), payload.greasedLightning());
            context.player().setAttached(PlayerMonkAttachments.greasedLightningExpires(), payload.greasedLightningExpires());
            context.player().setAttached(PlayerMonkAttachments.chakra(), payload.chakra());
        });
    }

}
