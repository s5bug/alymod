package tf.bug.alymod.network;

import com.mojang.serialization.Codec;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.item.EclipticClaw;

public record EclipticClawUsePayload(UUID sourceUuid) implements CustomPayload {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "ecliptic_claw_use");

    public static final Id<EclipticClawUsePayload> PAYLOAD_ID =
            new CustomPayload.Id<>(EclipticClawUsePayload.ID);

    public static final PacketCodec<RegistryByteBuf, EclipticClawUsePayload> CODEC =
            PacketCodec.tuple(Uuids.PACKET_CODEC, EclipticClawUsePayload::sourceUuid, EclipticClawUsePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return EclipticClawUsePayload.PAYLOAD_ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(EclipticClawUsePayload.PAYLOAD_ID, EclipticClawUsePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(EclipticClawUsePayload.PAYLOAD_ID, (payload, context) -> {
            context.player().getWorld().playSoundFromEntity(
                    context.player(),
                    context.player(),
                    EclipticClaw.CLIMB_SOUND_EVENT,
                    SoundCategory.PLAYERS,
                    EclipticClaw.CLIMB_SOUND_VOLUME,
                    1.0f
            );
        });
    }

}
