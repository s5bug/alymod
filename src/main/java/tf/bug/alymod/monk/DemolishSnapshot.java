package tf.bug.alymod.monk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import tf.bug.alymod.effect.MonkStatusEffects;

public final record DemolishSnapshot(
        int duration,
        int d2,
        double buffSnapshot,
        double critChance,
        double critMul,
        double dhitChance
) {

    public static final Codec<DemolishSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(DemolishSnapshot::duration),
            Codec.INT.fieldOf("d2").forGetter(DemolishSnapshot::d2),
            Codec.DOUBLE.fieldOf("buff_snapshot").forGetter(DemolishSnapshot::buffSnapshot),
            Codec.DOUBLE.fieldOf("crit_chance").forGetter(DemolishSnapshot::critChance),
            Codec.DOUBLE.fieldOf("crit_mul").forGetter(DemolishSnapshot::critMul),
            Codec.DOUBLE.fieldOf("dhit_chance").forGetter(DemolishSnapshot::dhitChance)
    ).apply(instance, DemolishSnapshot::new));

    public static final PacketCodec<RegistryByteBuf, DemolishSnapshot> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, DemolishSnapshot::duration,
            PacketCodecs.INTEGER, DemolishSnapshot::d2,
            PacketCodecs.DOUBLE, DemolishSnapshot::buffSnapshot,
            PacketCodecs.DOUBLE, DemolishSnapshot::critChance,
            PacketCodecs.DOUBLE, DemolishSnapshot::critMul,
            PacketCodecs.DOUBLE, DemolishSnapshot::dhitChance,
            DemolishSnapshot::new
    );

    public DemolishSnapshot updateDuration() {
        if(duration == StatusEffectInstance.INFINITE) return this;
        else return new DemolishSnapshot(
                duration - 1,
                d2,
                buffSnapshot,
                critChance,
                critMul,
                dhitChance
        );
    }

}
