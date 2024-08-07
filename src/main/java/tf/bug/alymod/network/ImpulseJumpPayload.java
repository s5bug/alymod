package tf.bug.alymod.network;

import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.item.EclipticClaw;

public record ImpulseJumpPayload() implements CustomPayload {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "impulse_jump");

    public static final Id<ImpulseJumpPayload> PAYLOAD_ID =
            new Id<>(ImpulseJumpPayload.ID);

    public static final PacketCodec<RegistryByteBuf, ImpulseJumpPayload> CODEC =
            PacketCodec.unit(new ImpulseJumpPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ImpulseJumpPayload.PAYLOAD_ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ImpulseJumpPayload.PAYLOAD_ID, ImpulseJumpPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ImpulseJumpPayload.PAYLOAD_ID, (payload, context) -> {
            context.player().getWorld().playSoundFromEntity(
                    context.player(),
                    context.player(),
                    EclipticClaw.IMPULSE_SOUND_EVENT,
                    SoundCategory.PLAYERS,
                    EclipticClaw.IMPULSE_SOUND_VOLUME,
                    1.0f
            );
        });
    }

}
