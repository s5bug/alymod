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

public record EclipticClawUseMessage(UUID sourceUuid) implements FabricPacket {

    public static final Identifier ID =
            Identifier.of(Alymod.ID, "ecliptic_claw_use");

    public static final PacketType<EclipticClawUseMessage> TYPE =
            PacketType.create(EclipticClawUseMessage.ID, EclipticClawUseMessage::read);

    public static EclipticClawUseMessage read(PacketByteBuf pbb) {
        return new EclipticClawUseMessage(pbb.readUuid());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(sourceUuid);
    }

    @Override
    public PacketType<?> getType() {
        return EclipticClawUseMessage.TYPE;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(EclipticClawUseMessage.TYPE, (packet, player, responseSender) -> {
            player.getWorld().playSoundFromEntity(
                    player,
                    player,
                    EclipticClaw.CLIMB_SOUND_EVENT,
                    SoundCategory.PLAYERS,
                    EclipticClaw.CLIMB_SOUND_VOLUME,
                    1.0f
            );
        });
    }

}
