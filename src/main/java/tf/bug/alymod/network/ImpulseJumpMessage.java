package tf.bug.alymod.network;

import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import tf.bug.alymod.Alymod;
import tf.bug.alymod.item.EclipticClaw;

public record ImpulseJumpMessage(UUID sourceUuid) implements FabricPacket {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "impulse_jump");

    public static final PacketType<ImpulseJumpMessage> TYPE =
            PacketType.create(ImpulseJumpMessage.ID, ImpulseJumpMessage::read);

    public static ImpulseJumpMessage read(PacketByteBuf pbb) {
        return new ImpulseJumpMessage(pbb.readUuid());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(sourceUuid);
    }

    @Override
    public PacketType<?> getType() {
        return ImpulseJumpMessage.TYPE;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ImpulseJumpMessage.TYPE, (packet, player, responseSender) -> {
            player.getWorld().playSoundFromEntity(
                    player,
                    player,
                    EclipticClaw.IMPULSE_SOUND_EVENT,
                    SoundCategory.PLAYERS,
                    EclipticClaw.IMPULSE_SOUND_VOLUME,
                    1.0f
            );
        });
    }

}
