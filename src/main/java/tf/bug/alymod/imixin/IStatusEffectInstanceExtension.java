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

    UUID alymod$getMeditativeBrotherhoodApplier();
    void alymod$setMeditativeBrotherhoodApplier(UUID meditativeBrotherhoodApplier);
    DemolishSnapshot alymod$getDemolishSnapshot();
    void alymod$setDemolishSnapshot(DemolishSnapshot demolishSnapshot);

    public static final record DemolishSnapshot(
            UUID owner,
            int d2,
            double buffSnapshot,
            double critChance,
            double critMul,
            double dhitChance
    ) {

        public static final Codec<DemolishSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Uuids.CODEC.fieldOf("owner").forGetter(DemolishSnapshot::owner),
                Codec.INT.fieldOf("d2").forGetter(DemolishSnapshot::d2),
                Codec.DOUBLE.fieldOf("buff_snapshot").forGetter(DemolishSnapshot::buffSnapshot),
                Codec.DOUBLE.fieldOf("crit_chance").forGetter(DemolishSnapshot::critChance),
                Codec.DOUBLE.fieldOf("crit_mul").forGetter(DemolishSnapshot::critMul),
                Codec.DOUBLE.fieldOf("dhit_chance").forGetter(DemolishSnapshot::dhitChance)
        ).apply(instance, DemolishSnapshot::new));

        public static final PacketCodec<RegistryByteBuf, DemolishSnapshot> PACKET_CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, DemolishSnapshot::owner,
                PacketCodecs.INTEGER, DemolishSnapshot::d2,
                PacketCodecs.DOUBLE, DemolishSnapshot::buffSnapshot,
                PacketCodecs.DOUBLE, DemolishSnapshot::critChance,
                PacketCodecs.DOUBLE, DemolishSnapshot::critMul,
                PacketCodecs.DOUBLE, DemolishSnapshot::dhitChance,
                DemolishSnapshot::new
        );

    }

}
