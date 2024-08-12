package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.bug.alymod.CodecUtil;
import tf.bug.alymod.effect.MonkStatusEffects;
import tf.bug.alymod.imixin.IStatusEffectInstanceExtension;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements IStatusEffectInstanceExtension {

    @Unique
    private static final String MEDITATIVE_BROTHERHOOD_APPLIERS_KEY =
            "alymod$meditative_brotherhood_appliers";

    @Unique
    private static final String DEMOLISH_SNAPSHOTS_KEY =
            "alymod$demoish_snapshots";

    @ModifyExpressionValue(
            at = @At(
                    value = "INVOKE",
                    remap = false,
                    target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
            ),
            method = "<clinit>"
    )
    private static Codec<StatusEffectInstance> addCustomFieldsCodec(
            Codec<StatusEffectInstance> value) {
        MapCodec.MapCodecCodec<StatusEffectInstance> mcc = (MapCodec.MapCodecCodec<StatusEffectInstance>) value;
        MapCodec<StatusEffectInstance> mc = mcc.codec();

        MapCodec<StatusEffectInstance> withMeditativeBrotherhood = CodecUtil.extend(
                mc,
                si -> si.getEffectType().matchesId(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.id()),
                MEDITATIVE_BROTHERHOOD_APPLIERS_KEY,
                StatusEffectInstance::new,
                Uuids.CODEC,
                si -> ((IStatusEffectInstanceExtension) si).alymod$getMeditativeBrotherhoodApplier(),
                (si, uuid) -> ((IStatusEffectInstanceExtension) si).alymod$setMeditativeBrotherhoodApplier(uuid)
        );
        MapCodec<StatusEffectInstance> withDemolish = CodecUtil.extend(
                withMeditativeBrotherhood,
                si -> si.getEffectType().matchesId(MonkStatusEffects.DEMOLISH.id()),
                DEMOLISH_SNAPSHOTS_KEY,
                StatusEffectInstance::new,
                DemolishSnapshot.CODEC,
                si -> ((IStatusEffectInstanceExtension) si).alymod$getDemolishSnapshot(),
                (si, snapshot) -> ((IStatusEffectInstanceExtension) si).alymod$setDemolishSnapshot(snapshot)
        );

        return withDemolish.codec();
    }

    @ModifyExpressionValue(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Ljava/util/function/BiFunction;)Lnet/minecraft/network/codec/PacketCodec;"
            ),
            method = "<clinit>"
    )
    private static PacketCodec<RegistryByteBuf, StatusEffectInstance> addCustomFieldsPacketCodec(PacketCodec<RegistryByteBuf, StatusEffectInstance> original) {
        PacketCodec<RegistryByteBuf, StatusEffectInstance> withMeditativeBrotherhood = CodecUtil.extendPacket(
                original,
                si -> si.getEffectType().matchesId(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.id()),
                StatusEffectInstance::new,
                Uuids.PACKET_CODEC,
                si -> ((IStatusEffectInstanceExtension) si).alymod$getMeditativeBrotherhoodApplier(),
                (si, uuid) -> ((IStatusEffectInstanceExtension) si).alymod$setMeditativeBrotherhoodApplier(uuid)
        );
        PacketCodec<RegistryByteBuf, StatusEffectInstance> withDemolish = CodecUtil.extendPacket(
                withMeditativeBrotherhood,
                si -> si.getEffectType().matchesId(MonkStatusEffects.DEMOLISH.id()),
                StatusEffectInstance::new,
                DemolishSnapshot.PACKET_CODEC,
                si -> ((IStatusEffectInstanceExtension) si).alymod$getDemolishSnapshot(),
                (si, snapshot) -> ((IStatusEffectInstanceExtension) si).alymod$setDemolishSnapshot(snapshot)
        );
        return withDemolish;
    }

    @Unique
    private UUID meditativeBrotherhoodApplier;
    @Unique
    private DemolishSnapshot demolishSnapshot;

    @Override
    public UUID alymod$getMeditativeBrotherhoodApplier() {
        return this.meditativeBrotherhoodApplier;
    }

    @Override
    public void alymod$setMeditativeBrotherhoodApplier(UUID uuid) {
        this.meditativeBrotherhoodApplier = uuid;
    }

    @Override
    public DemolishSnapshot alymod$getDemolishSnapshot() {
        return this.demolishSnapshot;
    }

    @Override
    public void alymod$setDemolishSnapshot(DemolishSnapshot demolishSnapshot) {
        this.demolishSnapshot = demolishSnapshot;
    }

}
