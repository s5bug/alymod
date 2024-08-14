package tf.bug.alymod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.*;
import java.util.function.BiConsumer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.bug.alymod.CodecUtil;
import tf.bug.alymod.effect.MonkStatusEffects;
import tf.bug.alymod.imixin.IStatusEffectInstanceExtension;
import tf.bug.alymod.monk.ExtendedStatusEffectInstance;
import tf.bug.alymod.monk.ExtendedStatusEffectType;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements IStatusEffectInstanceExtension {

    @Unique
    @Nullable
    private static ExtendedStatusEffectType<?> extensionForInstance(StatusEffectInstance instance) {
        RegistryKey<StatusEffect> effectIdKey = instance.getEffectType().getKey().orElse(null);
        if(effectIdKey != null) {
            Identifier effectId = effectIdKey.getValue();
            return ExtendedStatusEffectType.REGISTRY.get(effectId);
        } else return null;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static <U> U uncheckedGetExtension(StatusEffectInstance sei) {
        return (U) ((IStatusEffectInstanceExtension) sei).alymod$getExtension();
    }

    @Unique
    private static <U> void uncheckedSetExtension(StatusEffectInstance sei, U ext) {
        ((IStatusEffectInstanceExtension) sei).alymod$setExtension(ext);
    }

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

        MapCodec<StatusEffectInstance> withExtended = CodecUtil.extend(
                mc,
                StatusEffectInstanceMixin::extensionForInstance,
                "alymod$status_effect_instance_extension",
                StatusEffectInstance::new,
                ExtendedStatusEffectType::codec,
                StatusEffectInstanceMixin::uncheckedGetExtension,
                StatusEffectInstanceMixin::uncheckedSetExtension
        );

        return withExtended.codec();
    }

    @ModifyExpressionValue(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Ljava/util/function/BiFunction;)Lnet/minecraft/network/codec/PacketCodec;"
            ),
            method = "<clinit>"
    )
    private static PacketCodec<RegistryByteBuf, StatusEffectInstance> addCustomFieldsPacketCodec(PacketCodec<RegistryByteBuf, StatusEffectInstance> original) {
        PacketCodec<RegistryByteBuf, StatusEffectInstance> withExtended = CodecUtil.extendPacket(
                original,
                StatusEffectInstanceMixin::extensionForInstance,
                StatusEffectInstance::new,
                ExtendedStatusEffectType::packetCodec,
                StatusEffectInstanceMixin::uncheckedGetExtension,
                StatusEffectInstanceMixin::uncheckedSetExtension
        );
        return withExtended;
    }

    @Inject(
            at = @At(
                    value = "RETURN"
            ),
            method = "copyFrom"
    )
    public void copyExtension(StatusEffectInstance instance, CallbackInfo ci) {
        this.alymod$setExtension(((IStatusEffectInstanceExtension) instance).alymod$getExtension());
    }

    @Inject(
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/entity/effect/StatusEffectInstance;updateDuration()I"
            ),
            method = "update"
    )
    public void updateExtension(LivingEntity entity, Runnable overwriteCallback, CallbackInfoReturnable<Boolean> cir) {
        StatusEffectInstance thisx = (StatusEffectInstance) (Object) this;
        ExtendedStatusEffectType<?> extendedStatusEffectType = StatusEffectInstanceMixin.extensionForInstance(thisx);
        if(extendedStatusEffectType != null) {
            boolean shouldUpdate =
                    extendedStatusEffectType.sendUpdateOnTick().test(entity, new ExtendedStatusEffectInstance<>(thisx));
            if(shouldUpdate) overwriteCallback.run();
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static <T, U> void uncheckedCallBiConsumer(
            BiConsumer<T, U> consumer,
            Object left,
            Object right
    ) {
        consumer.accept((T) left, (U) right);
    }

    @Inject(
            at = @At(
                    value = "HEAD"
            ),
            method = "upgrade",
            cancellable = true
    )
    public void upgradeExtension(StatusEffectInstance that, CallbackInfoReturnable<Boolean> cir) {
        StatusEffectInstance thisx = (StatusEffectInstance) (Object) this;
        if(thisx.getEffectType().equals(that.getEffectType())) {
            ExtendedStatusEffectType<?> extendedStatusEffectType = StatusEffectInstanceMixin.extensionForInstance(thisx);
            if(extendedStatusEffectType != null) {
                @Nullable BiConsumer<? extends ExtendedStatusEffectInstance<?>, ? extends ExtendedStatusEffectInstance<?>> merger =
                        extendedStatusEffectType.modifyLeftMergeRight();
                if(merger != null) {
                    uncheckedCallBiConsumer(
                            merger,
                            new ExtendedStatusEffectInstance<>(thisx),
                            new ExtendedStatusEffectInstance<>(that)
                    );
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Unique
    private Object extension;

    @Override
    public Object alymod$getExtension() {
        return this.extension;
    }

    @Override
    public void alymod$setExtension(Object object) {
        this.extension = object;
    }

}
