package tf.bug.alymod.monk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import tf.bug.alymod.Alymod;

public final record ExtendedStatusEffectType<T>(
        Codec<T> codec,
        PacketCodec<RegistryByteBuf, T> packetCodec,
        @Nullable BiConsumer<ExtendedStatusEffectInstance<T>, ExtendedStatusEffectInstance<T>> modifyLeftMergeRight,
        BiPredicate<LivingEntity, ExtendedStatusEffectInstance<T>> sendUpdateOnTick
) {

    public static final Registry<ExtendedStatusEffectType<?>> REGISTRY =
            new SimpleRegistry<>(
                    RegistryKey.ofRegistry(Identifier.of(Alymod.ID, "extended_status_effect_types")),
                    Lifecycle.stable()
            );

}
