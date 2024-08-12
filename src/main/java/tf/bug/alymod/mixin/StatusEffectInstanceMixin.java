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

        MapCodec<StatusEffectInstance> next = mc.mapResult(new MapCodec.ResultFunction<>() {
            @Override
            public <T> DataResult<StatusEffectInstance> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<StatusEffectInstance> a) {
                return a.flatMap(x -> {
                    if(x.getEffectType().matchesId(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.id())) {
                        T mbaEncoded = input.get(MEDITATIVE_BROTHERHOOD_APPLIERS_KEY);
                        // TODO check if this is correct
                        DataResult<Pair<UUID, T>> decoded = Uuids.CODEC.decode(ops, mbaEncoded);
                        return decoded.map(p -> {
                            // TODO not sure if I have to do a copy
                            StatusEffectInstance copy = new StatusEffectInstance(x);
                            IStatusEffectInstanceExtension next = (IStatusEffectInstanceExtension) copy;
                            next.alymod$setMeditativeBrotherhoodApplier(p.getFirst());
                            return copy;
                        });
                    } else if(x.getEffectType().matchesId(MonkStatusEffects.DEMOLISH.id())) {
                        T demoEncoded = input.get(DEMOLISH_SNAPSHOTS_KEY);
                        // TODO check if this is correct
                        DataResult<Pair<DemolishSnapshot, T>> decoded = DemolishSnapshot.CODEC.decode(ops, demoEncoded);
                        return decoded.map(p -> {
                            // TODO not sure if I have to do a copy
                            StatusEffectInstance copy = new StatusEffectInstance(x);
                            IStatusEffectInstanceExtension next = (IStatusEffectInstanceExtension) copy;
                            next.alymod$setDemolishSnapshot(p.getFirst());
                            return copy;
                        });
                    } else return DataResult.success(x);
                });
            }

            @Override
            public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, StatusEffectInstance input, RecordBuilder<T> t) {
                if(input.getEffectType().matchesId(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.id())) {
                    IStatusEffectInstanceExtension inputExt = (IStatusEffectInstanceExtension) input;
                    UUID mba = inputExt.alymod$getMeditativeBrotherhoodApplier();
                    return t.add(MEDITATIVE_BROTHERHOOD_APPLIERS_KEY, mba, Uuids.CODEC);
                } else if(input.getEffectType().matchesId(MonkStatusEffects.DEMOLISH.id())) {
                    IStatusEffectInstanceExtension inputExt = (IStatusEffectInstanceExtension) input;
                    DemolishSnapshot demo = inputExt.alymod$getDemolishSnapshot();
                    return t.add(DEMOLISH_SNAPSHOTS_KEY, demo, DemolishSnapshot.CODEC);
                } else return t;
            }
        });

        return next.codec();
    }

    @ModifyExpressionValue(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Ljava/util/function/BiFunction;)Lnet/minecraft/network/codec/PacketCodec;"
            ),
            method = "<clinit>"
    )
    private static PacketCodec<RegistryByteBuf, StatusEffectInstance> addCustomFieldsPacketCodec(PacketCodec<RegistryByteBuf, StatusEffectInstance> original) {
        return new PacketCodec<>() {
            @Override
            public StatusEffectInstance decode(RegistryByteBuf buf) {
                StatusEffectInstance originalEffect = original.decode(buf);
                if(originalEffect.getEffectType().matchesId(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.id())) {
                    StatusEffectInstance copy = new StatusEffectInstance(originalEffect);
                    IStatusEffectInstanceExtension next = (IStatusEffectInstanceExtension) copy;
                    UUID decoded = Uuids.PACKET_CODEC.decode(buf);
                    next.alymod$setMeditativeBrotherhoodApplier(decoded);
                    return copy;
                } else if(originalEffect.getEffectType().matchesId(MonkStatusEffects.DEMOLISH.id())) {
                    StatusEffectInstance copy = new StatusEffectInstance(originalEffect);
                    IStatusEffectInstanceExtension next = (IStatusEffectInstanceExtension) copy;
                    DemolishSnapshot decoded = DemolishSnapshot.PACKET_CODEC.decode(buf);
                    next.alymod$setDemolishSnapshot(decoded);
                    return copy;
                } else return originalEffect;
            }

            @Override
            public void encode(RegistryByteBuf buf, StatusEffectInstance value) {
                original.encode(buf, value);
                if(value.getEffectType().matchesId(MonkStatusEffects.MEDITATIVE_BROTHERHOOD.id())) {
                    IStatusEffectInstanceExtension ext = (IStatusEffectInstanceExtension) value;
                    Uuids.PACKET_CODEC.encode(buf, ext.alymod$getMeditativeBrotherhoodApplier());
                } else if(value.getEffectType().matchesId(MonkStatusEffects.DEMOLISH.id())) {
                    IStatusEffectInstanceExtension ext = (IStatusEffectInstanceExtension) value;
                    DemolishSnapshot.PACKET_CODEC.encode(buf, ext.alymod$getDemolishSnapshot());
                } else {
                    return;
                }
            }
        };
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
