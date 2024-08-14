package tf.bug.alymod.imixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Pair;
import net.minecraft.util.Uuids;

public interface IStatusEffectInstanceExtension {

    Object alymod$getExtension();
    void alymod$setExtension(Object extension);

}
